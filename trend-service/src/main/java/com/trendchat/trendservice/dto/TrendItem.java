package com.trendchat.trendservice.dto;

/**
 * Google Trends 페이지에서 추출된 트렌드 키워드의 메타 정보를 담는 DTO입니다.
 * <p>
 * 이 객체는 크롤링 결과로부터 파싱된 추정 검색량, 상태, 시간 정보를 포함하며, Kafka 전송 또는 DB 저장을 위한 중간 데이터 구조로 사용됩니다.
 * </p>
 *
 * @param approxTraffic 해당 키워드의 추정 검색량 (예: 10000)
 * @param status        키워드의 상태 (예: '급상승', '상승 중', '활성')
 * @param time          수집된 시간 정보 (예: "4시간 전", "1시간 전")
 */
public record TrendItem(
        Integer approxTraffic,
        String status,
        String time
) {

}
