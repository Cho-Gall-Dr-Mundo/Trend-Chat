package com.trendchat.trendservice.repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Server-Sent Events(SSE) 연결을 관리하는 인메모리 저장소입니다.
 * <p>
 * {@link SseEmitter} 객체들을 thread-safe하게 보관하고 추가/제거할 수 있으며, 알림 전송 대상 구독자 목록을 관리하는 데 사용됩니다.
 * </p>
 *
 * <ul>
 *     <li>구독 요청 시 {@link #add(SseEmitter)}로 등록</li>
 *     <li>연결 종료 시 {@link #remove(SseEmitter)}로 제거</li>
 * </ul>
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.SseEmitter
 */
@Getter
@Repository
public class SseEmitterRepository {

    private final List<SseEmitter> emitterList = new CopyOnWriteArrayList<>();

    /**
     * 새로운 SSE 구독자 {@link SseEmitter}를 저장소에 추가합니다.
     *
     * @param sseEmitter 구독자 연결 객체
     */
    public void add(SseEmitter sseEmitter) {
        emitterList.add(sseEmitter);
    }

    /**
     * 저장소에서 SSE 구독자 {@link SseEmitter}를 제거합니다.
     *
     * @param sseEmitter 제거할 연결 객체
     */
    public void remove(SseEmitter sseEmitter) {
        emitterList.remove(sseEmitter);
    }
}
