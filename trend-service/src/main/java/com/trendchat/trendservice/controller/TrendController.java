package com.trendchat.trendservice.controller;

import com.trendchat.trendservice.dto.TrendResponse;
import com.trendchat.trendservice.service.TrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code TrendController}는 트렌드 키워드 및 뉴스 정보를 제공하는 REST API 컨트롤러입니다.
 *
 * <p>트렌드 검색, 뉴스 목록 조회, 단일 뉴스 상세 조회 등 다양한 API를 제공합니다.</p>
 *
 * <p>요청 필터링 및 정렬을 위한 쿼리 파라미터를 지원하며, 페이징 처리를 포함합니다.</p>
 *
 * @author TrendChat
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trends")
public class TrendController {

    private final TrendService trendService;

    /**
     * 트렌드 키워드 목록을 조회합니다.
     *
     * <p>대분류, 소분류, 검색어, 정렬 조건 등을 기반으로 트렌드를 필터링할 수 있으며,
     * 기본 정렬은 최신순(recent)입니다.</p>
     *
     * @param major    (선택) 대분류 카테고리 이름
     * @param sub      (선택) 소분류 카테고리 이름
     * @param search   (선택) 키워드 또는 요약 텍스트 검색어
     * @param sort     정렬 기준 ("recent", "oldest" 등)
     * @param pageable 페이지네이션 정보
     * @return 필터링된 트렌드 키워드 목록 (간단 정보)
     */
    @GetMapping
    public ResponseEntity<Page<TrendResponse.Simple>> getTrends(
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String sub,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "recent") String sort,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(trendService.searchTrends(major, sub, search, sort, pageable));
    }

    /**
     * 뉴스 요약이 포함된 트렌드 목록을 조회합니다.
     *
     * <p>대분류/소분류 또는 키워드 기반으로 필터링하며, 정렬 및 페이징을 지원합니다.</p>
     *
     * @param major    (선택) 대분류 카테고리
     * @param sub      (선택) 소분류 카테고리
     * @param search   (선택) 검색어
     * @param sort     정렬 방식
     * @param pageable 페이징 정보
     * @return 뉴스성 트렌드 목록
     */
    @GetMapping("/news")
    public ResponseEntity<Page<TrendResponse.Simple>> getNews(
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String sub,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "recent") String sort,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(trendService.searchNews(major, sub, search, sort, pageable));
    }

    /**
     * 단일 트렌드 키워드에 대한 뉴스 상세 정보를 조회합니다.
     *
     * <p>블로그 형식 본문, 요약, 카테고리 정보 등이 포함됩니다.</p>
     *
     * @param keyword 트렌드 키워드
     * @return 해당 키워드의 뉴스 상세 응답
     */
    @GetMapping("/news/{keyword}")
    public ResponseEntity<TrendResponse.Get> getSingleNews(@PathVariable String keyword) {
        return ResponseEntity.ok(trendService.getNews(keyword));
    }

    @GetMapping("/news/top6")
    public ResponseEntity<Page<TrendResponse.Get>> getTop6News() {
        return ResponseEntity.ok(trendService.getTop6News());
    }
}
