package com.trendchat.trendservice.dto;

import java.util.List;

public record TrendItem(
        String keyword,
        String summary,
        List<String> categories,
        String blog_post
) {

}