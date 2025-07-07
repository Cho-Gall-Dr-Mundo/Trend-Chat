package com.trendchat.trendservice.dto;

/**
 * 트렌드 키워드와 연관된 뉴스 기사의 주요 정보를 담는 DTO입니다.
 * <p>
 * 크롤링된 각 뉴스 카드에서 추출한 제목, 링크, 메타(출처 및 시간), 썸네일 이미지 URL을 포함합니다.<br>
 * <ul>
 *   <li>API 응답, Kafka 메시지, DB 저장 등에 활용</li>
 *   <li>뉴스 카드 내 텍스트 및 이미지 메타 정보를 구조적으로 전달</li>
 * </ul>
 * </p>
 *
 * @param title     기사 제목
 * @param url       기사 원문 링크(URL)
 * @param meta      출처 및 작성 시각 등 추가 메타 정보 (예: "5분 전 ● 조선일보")
 * @param thumbnail 기사 썸네일 이미지의 URL
 */
public record NewsItem(
        String title,
        String url,
        String meta,
        String thumbnail
) {

}
