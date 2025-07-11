package com.trendchat.trendservice.service;

import com.trendchat.trendservice.dto.TrendItem;
import com.trendchat.trendservice.dto.TrendKeywordItem;
import com.trendchat.trendservice.dto.TrendResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrendService {

    void processTrendResult(TrendItem item);

    void saveOrUpdateTrendAndProduce(String key, TrendKeywordItem value);

    Page<TrendResponse.Simple> searchTrends(
            String major,
            String sub,
            String search,
            String sort,
            Pageable pageable
    );
}
