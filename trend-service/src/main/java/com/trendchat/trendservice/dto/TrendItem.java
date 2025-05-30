package com.trendchat.trendservice.dto;

import java.util.List;

public record TrendItem(
        String approxTraffic,
        List<NewsItem> newsItems
) {

}
