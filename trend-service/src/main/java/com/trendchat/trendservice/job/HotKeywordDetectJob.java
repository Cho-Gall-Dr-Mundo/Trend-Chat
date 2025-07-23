package com.trendchat.trendservice.job;

import com.trendchat.trendservice.service.NotificationService;
import com.trendchat.trendservice.service.TrendKeywordServiceImpl;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 트렌드 키워드 중 급상승한 "HOT 키워드"를 감지하고 사용자에게 실시간 알림을 전송하는 Quartz Job 클래스입니다.
 * <p>
 * 이 Job은 {@link TrendKeywordServiceImpl}를 통해 HOT 키워드를 감지하고, {@link NotificationService}를 통해 해당 키워드를
 * 구독자에게 실시간 전파합니다.
 * </p>
 *
 * <ul>
 *     <li>HOT 키워드 감지 기준: 시간대별 검색량 변화 비율</li>
 *     <li>알림 방식: Server-Sent Events(SSE) 기반 {@code broadcastHotKeyword()}</li>
 * </ul>
 *
 * @see TrendKeywordServiceImpl
 * @see NotificationService
 */
@Component
@RequiredArgsConstructor
public class HotKeywordDetectJob implements Job {

    private final TrendKeywordServiceImpl trendKeywordService;
    private final NotificationService notificationService;

    /**
     * Quartz에 의해 주기적으로 실행되며 HOT 키워드를 감지하고 사용자에게 알림을 전송합니다.
     * <p>
     * 감지된 키워드 각각에 대해 {@link NotificationService#broadcastHotKeyword(String)}를 호출하여 전송합니다.
     * </p>
     *
     * @param context Quartz 실행 컨텍스트 (사용하지 않음)
     */
    @Override
    public void execute(JobExecutionContext context) {
        Set<String> hotKeywords = trendKeywordService.detectHotKeywords();

        for (String hotKeyword : hotKeywords) {
            notificationService.broadcastHotKeyword(hotKeyword);
        }
    }
}