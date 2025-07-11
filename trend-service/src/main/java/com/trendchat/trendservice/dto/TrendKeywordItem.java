package com.trendchat.trendservice.dto;

import java.util.List;

/**
 * Google Trends 페이지에서 추출된 트렌드 키워드의 메타 및 연관 뉴스 정보를 담는 DTO입니다.
 * <p>
 * 이 객체는 크롤링 결과로부터 파싱된 추정 검색량, 키워드 상태, 시간 정보와 함께 관련 뉴스 기사({@link NewsItem}) 목록을 포함합니다.<br>
 * <ul>
 *   <li>Kafka 전송, DB 저장, API 응답 등에서 공통적으로 사용하는 중간 데이터 구조로 활용</li>
 *   <li>news 필드에는 키워드와 직접적으로 연관된 최신 뉴스 기사들의 메타 정보가 포함됨</li>
 * </ul>
 * </p>
 *
 * @param approxTraffic 해당 키워드의 추정 검색량 (예: 10000)
 * @param status        키워드의 상태 (예: "급상승", "상승 중", "활성" 등)
 * @param time          데이터 수집 기준 시간 정보 (예: "4시간 전", "1시간 전" 등)
 * @param news          관련 뉴스 기사 정보({@link NewsItem}) 리스트
 * @see NewsItem
 */
public record TrendKeywordItem(
        Integer approxTraffic,
        String status,
        String time,
        List<NewsItem> news
) {

}
