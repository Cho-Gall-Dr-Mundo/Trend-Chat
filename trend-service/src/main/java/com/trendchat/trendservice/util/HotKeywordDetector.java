package com.trendchat.trendservice.util;

import java.util.HashSet;
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
 * </ul>
 */
@Component
public class HotKeywordDetector {

    private static final double HOT_THRESHOLD = 3.0;


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
}
