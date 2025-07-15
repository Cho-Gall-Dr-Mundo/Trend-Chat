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

/**
 * {@code TrendServiceImpl}는 트렌드 데이터의 저장, 갱신, 조회, 분류 업데이트를 담당하는 서비스 구현체입니다.
 *
 * <p>Kafka로 수신한 트렌드 키워드를 처리하거나, 사용자가 검색한 트렌드 뉴스 목록을 조회하는 로직이 포함됩니다.</p>
 *
 * <p>대부분의 메서드는 트랜잭션 범위 내에서 실행되며, 일부는 {@code @Transactional(readOnly = true)}로 성능 최적화를 고려합니다.</p>
 *
 * @author TrendChat
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendServiceImpl implements TrendService {

    private final TrendRepository trendRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final TrendKeywordProducer trendKeywordProducer;

    /**
     * Kafka를 통해 수신한 트렌드 분석 결과를 처리하고, 서브카테고리와 요약/블로그 정보를 트렌드 엔티티에 반영합니다.
     *
     * @param item 분석된 트렌드 항목 정보
     * @throws IllegalStateException 키워드에 해당하는 트렌드가 DB에 없을 경우
     */
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
            SubCategory subCategory = subCategoryRepository.findByName("특집기획").orElseThrow(
                    () -> new IllegalArgumentException("Not found subCategory"));
            trend.updateSubCategories(List.of(subCategory));
        }
        trend.updateSummaryAndBlogPost(item.summary(), item.blog_post());
    }

    /**
     * 트렌드 키워드를 저장하거나 업데이트한 뒤, Kafka 토픽에 전송합니다.
     * <ul>
     *   <li>기존 키워드가 있고 유입량이 증가한 경우 → 갱신 후 Kafka 전송</li>
     *   <li>새로운 키워드인 경우 → 저장 후 Kafka 전송</li>
     * </ul>
     *
     * @param key   트렌드 키워드
     * @param value 트렌드 키워드에 대한 상세 정보 (예: 유입량)
     */
    @Override
    @Transactional
    public void saveOrUpdateTrendAndProduce(
            String key,
            TrendKeywordItem value
    ) {
        Optional<Trend> optional = trendRepository.findByKeyword(key);
        if (optional.isPresent()) {
            Trend existing = optional.get();
            if (value.approxTraffic() >= 2000 && existing.getTraffic() < value.approxTraffic()) {
                existing.updateTraffic(value.approxTraffic());
                trendRepository.save(existing);
                trendKeywordProducer.sendKeyword(key, value);
            }
        } else {
            trendRepository.save(Trend.of(key, value.approxTraffic()));
            trendKeywordProducer.sendKeyword(key, value);
        }
    }

    /**
     * 조건에 따라 트렌드 키워드 목록을 검색합니다.
     *
     * @param major    대분류 카테고리 이름
     * @param sub      소분류 카테고리 이름
     * @param search   검색 키워드
     * @param sort     정렬 기준 (recent, oldest 등)
     * @param pageable 페이지 정보
     * @return 검색 결과 페이지 (간단 정보)
     */
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

    /**
     * 뉴스 요약이 포함된 트렌드 항목을 검색합니다.
     *
     * @param major    대분류 카테고리 이름
     * @param sub      소분류 카테고리 이름
     * @param search   검색 키워드
     * @param sort     정렬 기준 (recent, oldest 등)
     * @param pageable 페이지 정보
     * @return 뉴스 기반 트렌드 페이지 (간단 정보)
     */
    @Override
    public Page<TrendResponse.Simple> searchNews(
            String major,
            String sub,
            String search,
            String sort,
            Pageable pageable
    ) {
        return trendRepository.searchNews(major, sub, search, sort, pageable)
                .map(TrendResponse.Simple::new);
    }

    /**
     * 키워드를 기준으로 단건 뉴스 상세 정보를 조회합니다.
     *
     * @param keyword 트렌드 키워드
     * @return 해당 키워드에 대한 뉴스 상세 응답
     * @throws IllegalArgumentException 키워드에 해당하는 트렌드가 없을 경우
     */
    @Override
    public TrendResponse.Get getNews(String keyword) {
        Trend trend = trendRepository.findByKeyword(keyword).orElseThrow(
                () -> new IllegalArgumentException("Not found keyword"));
        return new TrendResponse.Get(trend);
    }
}
