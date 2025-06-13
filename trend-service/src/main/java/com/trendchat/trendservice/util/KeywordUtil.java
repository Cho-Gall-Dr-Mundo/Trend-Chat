package com.trendchat.trendservice.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class KeywordUtil {

    /**
     * 주어진 키워드 점수 맵을 정렬하여 상위 N개의 키워드를 반환합니다.
     *
     * @param scoreMap 키워드별 점수 맵
     * @param limit    반환할 개수 제한
     * @return 키워드와 트래픽을 포함한 Map 리스트
     */
    public List<Map<String, Object>> sortAndLimit(Map<String, Double> scoreMap, int limit) {
        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("keyword", e.getKey());
                    result.put("traffic", e.getValue().intValue());
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 키워드 리스트에서 keyword 값만 추출하여 Set으로 반환합니다.
     *
     * @param keywordList 키워드 Map 리스트
     * @return 키워드 문자열 Set
     */
    public Set<String> extractKeywords(List<Map<String, Object>> keywordList) {
        return keywordList.stream()
                .map(i -> (String) i.get("keyword"))
                .collect(Collectors.toSet());
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
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyyMMddHHmm").withZone(ZoneId.of("Asia/Seoul"));

        List<String> keys = new ArrayList<>();
        for (int i = startOffset; i < endOffset; i++) {
            Instant t = now.minusSeconds(i * 60L);
            keys.add("trend:" + formatter.format(t));
        }
        return keys;
    }
} 