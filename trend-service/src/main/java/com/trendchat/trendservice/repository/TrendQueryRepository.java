package com.trendchat.trendservice.repository;

import com.trendchat.trendservice.entity.Trend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrendQueryRepository {

    Page<Trend> searchTrends(
            String major,
            String sub,
            String search,
            String sort,
            Pageable pageable
    );
}
