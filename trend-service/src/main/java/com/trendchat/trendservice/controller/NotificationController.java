package com.trendchat.trendservice.controller;

import com.trendchat.trendservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 클라이언트의 실시간 알림 구독(SSE) 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 이 컨트롤러는 클라이언트가 Server-Sent Events(SSE)를 통해 서버로부터 비동기 이벤트(예: 급상승 키워드 알림)를 수신할 수 있도록 구독 엔드포인트를
 * 제공합니다.
 * </p>
 *
 * @see com.trendchat.trendservice.service.NotificationService
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * SSE 구독 엔드포인트입니다.
     * <p>
     * 클라이언트는 이 엔드포인트에 GET 요청을 보내면 {@link SseEmitter} 객체를 통해 서버로부터 실시간 이벤트 스트림을 수신할 수 있습니다.
     * </p>
     *
     * @return 서버-클라이언트 간 SSE 연결을 위한 {@link SseEmitter} 인스턴스
     */
    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return notificationService.subscribe();
    }
}
