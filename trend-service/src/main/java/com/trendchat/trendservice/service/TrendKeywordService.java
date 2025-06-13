package com.trendchat.trendservice.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 트렌드 키워드 관련 도메인 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 이 인터페이스는 트렌드 키워드의 수집 및 저장, 그리고 Redis를 활용한 급상승(HOT) 키워드 감지 기능을 제공합니다.
 * </p>
 *
 * @see com.trendchat.trendservice.service.TrendKeywordServiceImpl
 */
public interface TrendKeywordService {

    /**
     * 전달받은 키워드와 검색량을 기반으로 트렌드 키워드 데이터를 저장합니다.
     * <p>
     * 구현체에서는 DB에 영속화하거나, Redis에 키워드 점수를 추가하는 로직이 포함될 수 있습니다.
     * </p>
     *
     * @param keyword       수집된 트렌드 키워드 문자열
     * @param approxTraffic 해당 키워드의 추정 검색량 (예: 10000)
     */
    void createTrendKeyword(String keyword, int approxTraffic);

    List<Map<String, Object>> getTop10Keywords();

    /**
     * Redis에 저장된 최근 데이터와 과거 데이터를 비교하여 급상승한 키워드를 감지합니다.
     * <p>
     * 내부적으로 Redis ZSet 점수 데이터를 비교하여 통계 기반으로 HOT 키워드를 식별합니다.
     * </p>
     *
     * @return 감지된 HOT 키워드들의 Set
     */
    Set<String> detectHotKeywords();
}
