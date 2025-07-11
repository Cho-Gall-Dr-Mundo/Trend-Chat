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

    private Trend(String keyword, int traffic) {
        this.keyword = keyword;
        this.traffic = traffic;
    }

    public static Trend of(String keyword, int traffic) {
        return new Trend(keyword, traffic);
    }

    public void updateTraffic(int newTraffic) {
        this.traffic = newTraffic;
    }

    public void updateSubCategories(List<SubCategory> subCategories) {
        // 기존 연결 모두 삭제
        this.subCategoryLinks.clear();

        if (subCategories != null) {
            for (SubCategory sub : subCategories) {
                this.subCategoryLinks.add(TrendSubCategoryMap.of(this, sub));
            }
        }
    }

    public void updateSummaryAndBlogPost(String summary, String blogPost) {
        this.summary = summary;
        this.blogPost = blogPost;
    }
}
