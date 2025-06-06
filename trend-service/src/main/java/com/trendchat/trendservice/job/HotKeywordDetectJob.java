package com.trendchat.trendservice.job;

import com.trendchat.trendservice.service.TrendKeywordService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 트렌드 키워드 중 실시간으로 급상승한 "HOT 키워드"를 감지하는 Quartz Job 클래스입니다.
 * <p>
 * 이 Job은 {@link com.trendchat.trendservice.service.TrendKeywordService}의
 * {@code detectHotKeywords()} 메서드를 호출하여 특정 기준(예: 시간대별 변화량 등)에 따라 핫 키워드를 감지합니다.
 * <br>
 * 감지된 핫 키워드는 이후 알림 발송, 채팅방 생성 등 다양한 이벤트 트리거에 활용될 수 있습니다.
 * </p>
 *
 * @see com.trendchat.trendservice.service.TrendKeywordService
 */
@Component
@RequiredArgsConstructor
public class HotKeywordDetectJob implements Job {

    private final TrendKeywordService trendKeywordService;

    @Override
    public void execute(JobExecutionContext context) {
        Set<String> hotKeywords = trendKeywordService.detectHotKeywords();
        // TODO: 알림 기능 구현
    }
}