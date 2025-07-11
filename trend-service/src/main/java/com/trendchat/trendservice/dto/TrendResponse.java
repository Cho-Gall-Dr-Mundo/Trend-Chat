package com.trendchat.trendservice.dto;

import com.trendchat.trendservice.entity.Trend;

public sealed interface TrendResponse permits TrendResponse.Simple, TrendResponse.Get {

    record Simple(
            String keyword,
            String summary,
            String majorCategory,
            String subCategory
    ) implements TrendResponse {

        public Simple(Trend trend) {
            this(
                    trend.getKeyword(),
                    trend.getSummary(),
                    extractMajorCategory(trend),
                    extractSubCategory(trend)
            );
        }
    }

    record Get(
            String keyword,
            String summary,
            String blog_post,
            String majorCategory,
            String subCategory
    ) implements TrendResponse {

        public Get(Trend trend) {
            this(
                    trend.getKeyword(),
                    trend.getSummary(),
                    trend.getBlogPost(),
                    extractMajorCategory(trend),
                    extractSubCategory(trend)
            );
        }
    }

    private static String extractSubCategory(Trend trend) {
        return trend.getSubCategoryLinks().stream()
                .findFirst()
                .map(link -> link.getSubCategory().getName())
                .orElse(null);
    }

    private static String extractMajorCategory(Trend trend) {
        return trend.getSubCategoryLinks().stream()
                .findFirst()
                .map(link -> link.getSubCategory().getMajorCategory().getName())
                .orElse(null);
    }
}
