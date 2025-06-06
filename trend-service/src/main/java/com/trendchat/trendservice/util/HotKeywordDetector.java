package com.trendchat.trendservice.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Redis에 저장된 시간대별 트렌드 키워드 데이터를 기반으로 급상승(HOT) 키워드를 감지하는 유틸리티 클래스입니다.
 * <p>
 * 이전 시간대와 최근 시간대의 키워드 등장 횟수를 비교하여 상승률이 일정 임계치({@code HOT_THRESHOLD}) 이상인 키워드를 HOT으로 판단합니다. 또한, 시간대별
 * Redis ZSet 키를 생성할 수 있는 메서드도 제공합니다.
 * </p>
 *
 * <ul>
 *     <li>{@link #detect(Map, Map)}: 최근 vs 과거 비교로 HOT 키워드 감지</li>
 *     <li>{@link #getMinuteKeys(Instant, int, int)}: Redis 시간 키 목록 생성</li>
 * </ul>
 */
@Component
public class HotKeywordDetector {

    private static final double HOT_THRESHOLD = 3.0;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneId.of("Asia/Seoul"));

    /**
     * 주어진 최근/과거 키워드 점수를 비교하여 급상승한 키워드를 감지합니다.
     * <p>
     * 과거 대비 등장 횟수가 {@code HOT_THRESHOLD} 이상 증가한 키워드를 HOT 키워드로 판단합니다.
     * </p>
     *
     * @param recent 최근 5분 검색량 맵
     * @param past   과거 5분 검색량 맵
     * @return 급상승한 키워드들의 집합
     */
    public Set<String> detect(Map<String, Integer> recent, Map<String, Integer> past) {
        Set<String> hotKeywords = new HashSet<>();

        for (String keyword : recent.keySet()) {
            int r = recent.getOrDefault(keyword, 0);
            int p = past.getOrDefault(keyword, 0);

            if (p > 0 && ((double) r / p) >= HOT_THRESHOLD) {
                hotKeywords.add(keyword);
            }
        }

        return hotKeywords;
    }

    /**
     * 분 단위 기준 시간 오프셋을 기반으로 Redis ZSet 키 목록을 생성합니다.
     * <p>
     * 예: trend:202406061230 ~ trend:202406061234 와 같은 키 목록 생성
     * </p>
     *
     * @param now         기준 시간 (보통 현재 시각)
     * @param startOffset 시작 offset (분 단위, inclusive)
     * @param endOffset   종료 offset (분 단위, exclusive)
     * @return Redis ZSet 키 리스트
     */
    public List<String> getMinuteKeys(Instant now, int startOffset, int endOffset) {
        List<String> keys = new ArrayList<>();
        for (int i = startOffset; i < endOffset; i++) {
            Instant t = now.minusSeconds(i * 60L);
            keys.add("trend:" + FORMATTER.format(t));
        }
        return keys;
    }
}
