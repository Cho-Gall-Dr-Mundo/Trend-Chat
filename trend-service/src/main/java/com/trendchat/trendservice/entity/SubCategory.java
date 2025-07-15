package com.trendchat.trendservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * {@code SubCategory} 엔티티는 트렌드 키워드의 소분류를 나타내며, {@link MajorCategory}와의 연관 관계를 통해 상위 분류를 구성합니다.
 *
 * <p>하나의 소분류는 여러 개의 {@link Trend}와 매핑될 수 있으며,
 * 중간 테이블인 {@link TrendSubCategoryMap}을 통해 다대다 관계를 구성합니다.</p>
 *
 * @author TrendChat
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sub_category")
public class SubCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_category_id")
    private MajorCategory majorCategory;

    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrendSubCategoryMap> trendLinks = new ArrayList<>();

    /**
     * SubCategory 생성자
     *
     * @param name          소분류 이름
     * @param majorCategory 소속 대분류
     */
    public SubCategory(String name, MajorCategory majorCategory) {
        this.name = name;
        this.majorCategory = majorCategory;
    }
}
