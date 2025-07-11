package com.trendchat.trendservice.controller;

import com.trendchat.trendservice.dto.TrendResponse;
import com.trendchat.trendservice.service.TrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trends")
public class TrendController {

    private final TrendService trendService;

    @GetMapping
    public Page<TrendResponse.Simple> getTrends(
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String sub,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "recent") String sort,
            @PageableDefault Pageable pageable
    ) {
        return trendService.searchTrends(major, sub, search, sort, pageable);
    }
}
