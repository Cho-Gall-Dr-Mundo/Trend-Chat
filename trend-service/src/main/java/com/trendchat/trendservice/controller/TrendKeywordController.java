package com.trendchat.trendservice.controller;

import com.trendchat.trendservice.service.TrendKeywordService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 트렌드 키워드 관련 API 엔드포인트를 제공하는 컨트롤러입니다.
 * <p>
 * 실시간 인기 트렌드 키워드, 뉴스, 메타 정보 등을 외부에 제공하기 위한 RESTful API를 구현합니다.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trends")
public class TrendKeywordController {

    private final TrendKeywordService trendKeywordService;

    /**
     * 실시간 상위 10개 트렌드 키워드 및 관련 정보를 조회하는 API입니다.
     *
     * @return 상위 10개 트렌드 키워드 및 메타 정보(JSON) 리스트 (HTTP 200)
     * <ul>
     *   <li>각 항목은 {@code Map<String, Object>} 구조로 키워드, 검색량, 뉴스 등 포함</li>
     * </ul>
     */
    @GetMapping("/top10")
    public ResponseEntity<List<Map<String, Object>>> getTop10Keywords() {
        return ResponseEntity.status(HttpStatus.OK).body(trendKeywordService.getTop10Keywords());
    }
}
