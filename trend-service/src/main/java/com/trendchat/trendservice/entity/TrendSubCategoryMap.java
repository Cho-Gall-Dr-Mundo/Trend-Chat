package com.trendchat.trendservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * {@code TrendSubCategoryMap} 엔티티는 {@link Trend}와 {@link SubCategory} 간의 다대다(N:N) 관계를 표현하는 중간 매핑
 * 테이블 역할을 합니다.
 *
 * <p>하나의 트렌드는 여러 서브카테고리에 속할 수 있고, 하나의 서브카테고리도 여러 트렌드에 속할 수 있습니다.</p>
 * <p>이 매핑 테이블은 연관관계 엔티티로 분리되어 있어, 향후 추가 메타데이터(예: 분류 신뢰도, 우선순위 등)를 확장할 수 있는 구조입니다.</p>
 *
 * @author TrendChat
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trend_subcategory_map")
public class TrendSubCategoryMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trend_id")
    private Trend trend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    /**
     * TrendSubCategoryMap 생성자 (정적 팩토리 사용 권장)
     *
     * @param trend       연관 트렌드
     * @param subCategory 연관 서브카테고리
     */
    private TrendSubCategoryMap(Trend trend, SubCategory subCategory) {
        this.trend = trend;
        this.subCategory = subCategory;
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param trend       트렌드
     * @param subCategory 서브카테고리
     * @return 새 매핑 인스턴스
     */
    public static TrendSubCategoryMap of(Trend trend, SubCategory subCategory) {
        return new TrendSubCategoryMap(trend, subCategory);
    }
}
