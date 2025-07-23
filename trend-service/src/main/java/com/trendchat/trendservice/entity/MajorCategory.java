package com.trendchat.trendservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * {@code MajorCategory} 엔티티는 트렌드 키워드 분류 체계에서 최상위 대분류를 나타내는 도메인입니다.
 *
 * <p>예: "정치", "경제", "IT/과학", "연예", "스포츠" 등</p>
 *
 * <p>{@link SubCategory}와 1:N 관계를 맺으며,
 * 실제 데이터 조회 시 소분류를 통해 트렌드와 연결됩니다.</p>
 *
 * @author TrendChat
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "major_category")
public class MajorCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    /**
     * MajorCategory 생성자
     *
     * @param name 대분류 이름
     */
    public MajorCategory(String name) {
        this.name = name;
    }
}
