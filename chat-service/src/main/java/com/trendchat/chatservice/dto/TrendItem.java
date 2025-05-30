package com.trendchat.chatservice.dto;

import java.util.List;

public record TrendItem(
        String approxTraffic,
        List<NewsItem> newsItems
) {

}
