package com.trendchat.trendservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Google Trends에서 수집된 트렌드 키워드 정보를 저장하는 JPA 엔티티입니다.
 * <p>
 * 각 트렌드 키워드는 키워드명, 추정 검색량, 수집 시각 등의 정보를 포함하며, {@code trend_keyword} 테이블에 저장됩니다.
 * </p>
 *
 * <ul>
 *     <li>{@code keyword} — 트렌드 키워드 문자열</li>
 *     <li>{@code approxTraffic} — 추정 검색량 (예: 10,000)</li>
 *     <li>{@code createdAt} — 키워드 수집 시각 (자동 생성)</li>
 * </ul>
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trend_keyword")
public class TrendKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    private Integer approxTraffic;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 내부 생성자. 정적 팩토리 메서드를 통해 객체를 생성합니다.
     *
     * @param keyword       트렌드 키워드
     * @param approxTraffic 해당 키워드의 추정 검색량
     */
    private TrendKeyword(String keyword, int approxTraffic) {
        this.keyword = keyword;
        this.approxTraffic = approxTraffic;
    }

    /**
     * Google Trend Crawler로부터 받은 트렌드 키워드 정보를 바탕으로 {@link TrendKeyword} 인스턴스를 생성합니다.
     *
     * @param keyword       트렌드 키워드명
     * @param approxTraffic 추정 검색량
     * @return {@link TrendKeyword} 객체
     */
    public static TrendKeyword of(String keyword, int approxTraffic) {
        return new TrendKeyword(keyword, approxTraffic);
    }
}