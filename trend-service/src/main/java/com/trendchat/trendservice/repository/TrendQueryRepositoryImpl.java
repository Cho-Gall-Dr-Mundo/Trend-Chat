package com.trendchat.trendservice.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trendchat.trendservice.entity.QMajorCategory;
import com.trendchat.trendservice.entity.QSubCategory;
import com.trendchat.trendservice.entity.QTrend;
import com.trendchat.trendservice.entity.QTrendSubCategoryMap;
import com.trendchat.trendservice.entity.Trend;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TrendQueryRepositoryImpl implements TrendQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Trend> searchTrends(
            String major,
            String sub,
            String search,
            String sort,
            Pageable pageable
    ) {
        QTrend trend = QTrend.trend;
        QTrendSubCategoryMap map = QTrendSubCategoryMap.trendSubCategoryMap;
        QSubCategory subCategory = QSubCategory.subCategory;
        QMajorCategory majorCategory = QMajorCategory.majorCategory;

        JPQLQuery<Trend> query;
        JPQLQuery<Long> countQuery;

        OrderSpecifier<?> orderSpecifier = switch (sort) {
            case "recent" -> trend.createdAt.desc();
            case "oldest" -> trend.createdAt.asc();
            default -> trend.createdAt.desc();
        };

        BooleanBuilder where = new BooleanBuilder();

        // 검색어 조건
        if (search != null && !search.isBlank()) {
            where.and(
                    trend.keyword.containsIgnoreCase(search)
                            .or(trend.summary.containsIgnoreCase(search))
            );
        }

        if (major == null && sub == null) {
            query = queryFactory
                    .selectFrom(trend)
                    .where(where)
                    .orderBy(orderSpecifier);

            countQuery = queryFactory
                    .select(trend.count())
                    .from(trend)
                    .where(where);
        } else {
            if (major != null) {
                where.and(majorCategory.name.eq(major));
            }
            if (sub != null) {
                where.and(subCategory.name.eq(sub));
            }

            query = queryFactory
                    .select(trend)
                    .from(trend)
                    .join(trend.subCategoryLinks, map)
                    .join(map.subCategory, subCategory)
                    .join(subCategory.majorCategory, majorCategory)
                    .where(where)
                    .orderBy(orderSpecifier)
                    .distinct();

            countQuery = queryFactory
                    .select(trend.countDistinct())
                    .from(trend)
                    .join(trend.subCategoryLinks, map)
                    .join(map.subCategory, subCategory)
                    .join(subCategory.majorCategory, majorCategory)
                    .where(where);
        }

        List<Trend> result = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public Page<Trend> searchNews(
            String major,
            String sub,
            String search,
            String sort,
            Pageable pageable
    ) {
        QTrend trend = QTrend.trend;
        QTrendSubCategoryMap map = QTrendSubCategoryMap.trendSubCategoryMap;
        QSubCategory subCategory = QSubCategory.subCategory;
        QMajorCategory majorCategory = QMajorCategory.majorCategory;

        JPQLQuery<Trend> query;
        JPQLQuery<Long> countQuery;

        OrderSpecifier<?> orderSpecifier = switch (sort) {
            case "recent" -> trend.createdAt.desc();
            case "oldest" -> trend.createdAt.asc();
            default -> trend.createdAt.desc();
        };

        BooleanBuilder where = new BooleanBuilder();

        // 블로그 요약이 있는 트렌드만
        where.and(trend.blogPost.isNotNull().and(trend.blogPost.length().gt(0)));

        // 검색어 필터
        if (search != null && !search.isBlank()) {
            where.and(
                    trend.keyword.containsIgnoreCase(search)
                            .or(trend.summary.containsIgnoreCase(search))
            );
        }

        if (major == null && sub == null) {
            query = queryFactory
                    .selectFrom(trend)
                    .where(where)
                    .orderBy(orderSpecifier);

            countQuery = queryFactory
                    .select(trend.count())
                    .from(trend)
                    .where(where);
        } else {
            if (major != null) {
                where.and(majorCategory.name.eq(major));
            }
            if (sub != null) {
                where.and(subCategory.name.eq(sub));
            }

            query = queryFactory
                    .select(trend)
                    .from(trend)
                    .join(trend.subCategoryLinks, map)
                    .join(map.subCategory, subCategory)
                    .join(subCategory.majorCategory, majorCategory)
                    .where(where)
                    .orderBy(orderSpecifier)
                    .distinct();

            countQuery = queryFactory
                    .select(trend.countDistinct())
                    .from(trend)
                    .join(trend.subCategoryLinks, map)
                    .join(map.subCategory, subCategory)
                    .join(subCategory.majorCategory, majorCategory)
                    .where(where);
        }

        List<Trend> result = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);

        return new PageImpl<>(result, pageable, total);
    }
}
