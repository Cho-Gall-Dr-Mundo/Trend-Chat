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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trends")
public class TrendKeywordController {

    private final TrendKeywordService trendKeywordService;

    @GetMapping("/top10")
    public ResponseEntity<List<Map<String, Object>>> getTop10Keywords() {
        return ResponseEntity.status(HttpStatus.OK).body(trendKeywordService.getTop10Keywords());
    }
}
