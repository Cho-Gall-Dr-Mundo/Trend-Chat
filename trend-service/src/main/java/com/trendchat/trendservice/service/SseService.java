package com.trendchat.trendservice.service;

import com.trendchat.trendservice.repository.SseEmitterRepository;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Server-Sent Events(SSE)를 기반으로 한 실시간 알림 전송 서비스 구현체입니다.
 * <p>
 * 클라이언트는 {@link #subscribe()}를 통해 SSE 연결을 맺고, 서버는 {@link #broadcastHotKeyword(String)}를 통해 특정
 * 이벤트("hot-keyword")를 브로드캐스팅합니다. 또한 연결 유지 및 프록시 타임아웃 방지를 위해 주기적으로 ping 메시지를 전송합니다.
 * </p>
 *
 * <p>
 * - 각 {@link SseEmitter}는 3분 동안 유효하며, 타임아웃 또는 연결 종료 시 자동으로 제거됩니다.<br> - ping 메시지는 15초 간격으로 전송되어 연결
 * 상태를 유지합니다.
 * </p>
 *
 * @see NotificationService
 * @see SseEmitter
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseService implements NotificationService {

    private final SseEmitterRepository sseEmitterRepository;

    /**
     * 클라이언트가 SSE 연결을 통해 알림을 구독할 수 있도록 {@link SseEmitter}를 생성하고 등록합니다.
     * <p>
     * 타임아웃: 3분<br> ping 주기: 15초
     * </p>
     *
     * @return 알림 전송을 위한 {@link SseEmitter} 인스턴스
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        sseEmitterRepository.add(emitter);

        emitter.onTimeout(() -> sseEmitterRepository.remove(emitter));
        emitter.onCompletion(() -> sseEmitterRepository.remove(emitter));

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("ping")); // 클라이언트에서 : ping 수신
            } catch (IOException e) {
                emitter.completeWithError(e);
                scheduler.shutdown(); // 에러 발생 시 스케줄러 중단
            }
        }, 0, 15, TimeUnit.SECONDS);

        return emitter;
    }

    /**
     * 수집된 HOT 키워드를 SSE를 통해 모든 구독자에게 브로드캐스트합니다.
     *
     * @param keyword 급상승 트렌드 키워드
     */
    public void broadcastHotKeyword(String keyword) {
        log.info("Broadcasting keyword: {}", keyword);
        for (SseEmitter emitter : sseEmitterRepository.getEmitterList()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("hot-keyword")
                        .data(keyword));
            } catch (IOException e) {
                emitter.completeWithError(e);
                sseEmitterRepository.remove(emitter);
            }
        }
    }
}
