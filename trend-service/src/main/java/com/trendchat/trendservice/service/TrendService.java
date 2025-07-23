package com.trendchat.trendservice.service;

import com.trendchat.trendservice.dto.TrendItem;
import com.trendchat.trendservice.dto.TrendKeywordItem;
import com.trendchat.trendservice.dto.TrendResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * {@code TrendService}는 트렌드 키워드와 관련된 데이터 처리, 검색, 뉴스 요약 제공 등의 주요 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 *
 * <p>Kafka, DB, 프론트엔드 간의 중간 계층 역할을 수행하며, 트렌드 기반의 기능을 확장하기 위한 핵심 계약입니다.</p>
 *
 * @author TrendChat
 */
public interface TrendService {

    /**
     * 트렌드 처리 결과를 반영하여, 해당 키워드의 카테고리 및 요약/블로그 정보를 업데이트합니다. 주로 Kafka 소비 후 실행됩니다.
     *
     * @param item 분석된 트렌드 데이터 항목
     */
    void processTrendResult(TrendItem item);

    /**
     * 키워드가 이미 존재하면 트래픽 정보를 비교해 업데이트하고, Kafka에 다시 전송합니다. 존재하지 않으면 새로 저장 후 전송합니다.
     *
     * @param key   트렌드 키워드
     * @param value 트렌드 키워드에 대한 메타 정보 (트래픽 포함)
     */
    void saveOrUpdateTrendAndProduce(String key, TrendKeywordItem value);

    /**
     * 대분류, 소분류, 검색어, 정렬 조건에 따라 트렌드 키워드를 페이징 조회합니다. 주로 트렌드 키워드 목록 페이지에서 사용됩니다.
     *
     * @param major    대분류 이름
     * @param sub      소분류 이름
     * @param search   검색어
     * @param sort     정렬 방식 ("recent", "oldest" 등)
     * @param pageable 페이지 요청 정보
     * @return 조건에 맞는 트렌드 키워드 목록 (간단 정보)
     */
    Page<TrendResponse.Simple> searchTrends(
            String major,
            String sub,
            String search,
            String sort,
            Pageable pageable
    );

    /**
     * 요약 및 블로그 정보가 포함된 뉴스성 트렌드 데이터를 검색합니다. 뉴스 탭 등에서 활용됩니다.
     *
     * @param major    대분류 이름
     * @param sub      소분류 이름
     * @param search   검색어
     * @param sort     정렬 방식 ("recent", "oldest" 등)
     * @param pageable 페이지 요청 정보
     * @return 조건에 맞는 뉴스형 트렌드 데이터 목록 (간단 정보)
     */
    Page<TrendResponse.Simple> searchNews(
            String major,
            String sub,
            String search,
            String sort,
            Pageable pageable
    );

    /**
     * 특정 키워드에 대한 상세 뉴스 정보를 조회합니다. 블로그 요약, 카테고리 등 상세 정보가 포함됩니다.
     *
     * @param keyword 트렌드 키워드
     * @return 해당 키워드의 상세 뉴스 응답 DTO
     */
    TrendResponse.Get getNews(String keyword);
}
