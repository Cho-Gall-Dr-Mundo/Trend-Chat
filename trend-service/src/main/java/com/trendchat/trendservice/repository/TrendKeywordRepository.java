package com.trendchat.trendservice.repository;

import com.trendchat.trendservice.entity.TrendKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link com.trendchat.trendservice.entity.TrendKeyword} 엔티티에 대한 JPA 리포지토리입니다.
 * <p>
 * 트렌드 키워드 데이터를 DB에 저장하거나 조회하는 기본 CRUD 작업을 지원하며, Spring Data JPA에 의해 구현체가 자동 생성됩니다.
 * </p>
 */
public interface TrendKeywordRepository extends JpaRepository<TrendKeyword, Long> {

}
