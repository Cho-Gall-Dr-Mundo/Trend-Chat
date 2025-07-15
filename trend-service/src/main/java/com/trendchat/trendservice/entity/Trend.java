package com.trendchat.trendservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * {@code Trend} 엔티티는 트렌드 키워드와 관련된 핵심 정보를 저장하는 도메인 객체입니다.
 *
 * <p>트렌드는 고유한 키워드, 트래픽 수치, 요약 정보, 블로그 형태의 설명글을 포함하며,
 * 복수의 {@link SubCategory}와 다대다 관계를 가집니다.</p>
 *
 * <p>서브카테고리는 중간 매핑 테이블인 {@link TrendSubCategoryMap}을 통해 연결됩니다.</p>
 *
 * @author TrendChat
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trend")
public class Trend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String keyword;

    private Integer traffic;

    @Column(length = 1000)
    private String summary;

    @OneToMany(mappedBy = "trend", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrendSubCategoryMap> subCategoryLinks = new ArrayList<>();

    @Column(length = 5000)
    private String blogPost;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * Trend 객체 생성자 (정적 팩토리 메서드 사용을 권장)
     *
     * @param keyword 트렌드 키워드
     * @param traffic 검색량 또는 유입 트래픽
     */
    private Trend(String keyword, int traffic) {
        this.keyword = keyword;
        this.traffic = traffic;
    }

    /**
     * Trend 정적 팩토리 메서드
     *
     * @param keyword 키워드
     * @param traffic 검색량
     * @return 생성된 {@code Trend} 객체
     */
    public static Trend of(String keyword, int traffic) {
        return new Trend(keyword, traffic);
    }

    /**
     * 트래픽 정보를 업데이트합니다.
     *
     * @param newTraffic 새로운 트래픽 값
     */
    public void updateTraffic(int newTraffic) {
        this.traffic = newTraffic;
    }

    /**
     * 서브카테고리 매핑을 업데이트합니다. 기존 연결은 모두 제거하고 새로운 매핑 리스트로 교체합니다.
     *
     * @param subCategories 연결할 서브카테고리 목록
     */
    public void updateSubCategories(List<SubCategory> subCategories) {
        this.subCategoryLinks.clear();

        if (subCategories != null) {
            for (SubCategory sub : subCategories) {
                this.subCategoryLinks.add(TrendSubCategoryMap.of(this, sub));
            }
        }
    }

    /**
     * 요약 정보 및 블로그 본문을 업데이트합니다.
     *
     * @param summary  뉴스 요약 텍스트
     * @param blogPost 마크다운 형식의 블로그 포스트
     */
    public void updateSummaryAndBlogPost(String summary, String blogPost) {
        this.summary = summary;
        this.blogPost = blogPost;
    }
}
