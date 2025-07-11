package com.trendchat.trendservice.service;

import com.trendchat.trendservice.dto.TrendItem;
import com.trendchat.trendservice.dto.TrendKeywordItem;
import com.trendchat.trendservice.dto.TrendResponse;
import com.trendchat.trendservice.entity.SubCategory;
import com.trendchat.trendservice.entity.Trend;
import com.trendchat.trendservice.repository.SubCategoryRepository;
import com.trendchat.trendservice.repository.TrendRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendServiceImpl implements TrendService {

    private final TrendRepository trendRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final TrendKeywordProducer trendKeywordProducer;

    @Override
    @Transactional
    public void processTrendResult(TrendItem item) {
        Trend trend = trendRepository.findByKeyword(item.keyword())
                .orElseThrow(
                        () -> new IllegalStateException("Not found Keyword: " + item.keyword()));
        
        if (item.categories() != null && !item.categories().isEmpty()) {
            List<SubCategory> subCategories = subCategoryRepository.findByNameIn(item.categories());
            trend.updateSubCategories(subCategories);
        } else {
            trend.updateSubCategories(null);
        }
        trend.updateSummaryAndBlogPost(item.summary(), item.blog_post());
    }

    @Override
    @Transactional
    public void saveOrUpdateTrendAndProduce(
            String key,
            TrendKeywordItem value
    ) {
        Optional<Trend> optional = trendRepository.findByKeyword(key);
        if (optional.isPresent()) {
            Trend existing = optional.get();
            if (value.approxTraffic() >= 2000) {
                existing.updateTraffic(value.approxTraffic());
                trendRepository.save(existing);
                trendKeywordProducer.send(key, value);
            }
        } else {
            trendRepository.save(Trend.of(key, value.approxTraffic()));
            trendKeywordProducer.send(key, value);
        }
    }

    @Override
    public Page<TrendResponse.Simple> searchTrends(
            String major,
            String sub,
            String search,
            String sort,
            Pageable pageable
    ) {
        return trendRepository.searchTrends(major, sub, search, sort, pageable)
                .map(TrendResponse.Simple::new);
    }
}
