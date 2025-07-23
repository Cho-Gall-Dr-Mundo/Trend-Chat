package com.trendchat.trendservice.repository;

import com.trendchat.trendservice.entity.Trend;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendRepository extends JpaRepository<Trend, Long>, TrendQueryRepository {

    Optional<Trend> findByKeyword(String keyword);
}
