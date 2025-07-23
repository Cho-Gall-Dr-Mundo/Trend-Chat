package com.trendchat.trendservice.dto;

import java.util.List;

/**
 * {@code TrendItem}은 외부 시스템 또는 AI 에이전트로부터 전달받는 트렌드 분석 결과를 담는 데이터 전송 객체(DTO)입니다.
 *
 * <p>Kafka 메시지 소비 시 사용되며, 트렌드 키워드에 대한 요약 정보, 카테고리 분류, 블로그 형태의 본문 데이터를 포함합니다.</p>
 *
 * @param keyword    트렌드 키워드 (예: "애플 신제품", "총선 결과")
 * @param summary    키워드와 관련된 간단한 뉴스 요약
 * @param categories 해당 키워드에 매핑된 서브카테고리 이름 목록
 * @param blog_post  블로그 형식의 마크다운 본문 (뉴스 내용 등)
 * @author TrendChat
 */
public record TrendItem(
        String keyword,
        String summary,
        List<String> categories,
        String blog_post
) {

}
