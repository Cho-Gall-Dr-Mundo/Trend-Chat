package com.trendchat.trendservice.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 실시간 알림 전송을 위한 서비스 인터페이스입니다.
 * <p>
 * 주로 트렌드 키워드의 급상승(HOT) 여부에 따른 알림을 사용자에게 전송하는 기능을 제공합니다. 이 인터페이스는 Server-Sent Events(SSE)를 기반으로
 * 구현됩니다.
 * </p>
 *
 * <ul>
 *     <li>{@link #subscribe()} — 클라이언트가 SSE 연결을 구독하도록 지원</li>
 *     <li>{@link #broadcastHotKeyword(String)} — 감지된 핫 키워드를 모든 구독자에게 전송</li>
 * </ul>
 */
public interface NotificationService {

    /**
     * 클라이언트가 실시간 알림을 수신할 수 있도록 SSE 연결을 구독합니다.
     *
     * @return 연결된 클라이언트에게 알림을 푸시하기 위한 {@link SseEmitter} 인스턴스
     */
    SseEmitter subscribe();

    /**
     * 감지된 HOT 키워드를 모든 구독 클라이언트에게 알림으로 전송합니다.
     *
     * @param keyword 실시간으로 감지된 급상승 트렌드 키워드
     */
    void broadcastHotKeyword(String keyword);
}
