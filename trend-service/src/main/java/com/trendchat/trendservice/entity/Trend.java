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
@Table(name = "trend_keyword")
public class Trend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    @Column(length = 1000)
    private String summary;

    private String majorCategory;

    @OneToMany(mappedBy = "trend", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrendSubCategoryMap> subCategoryLinks = new ArrayList<>();

    @Column(length = 5000)
    private String blogPost;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
