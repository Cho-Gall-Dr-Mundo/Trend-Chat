package com.trendchat.trendservice.repository;

import com.trendchat.trendservice.entity.Trend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendRepository extends JpaRepository<Trend, Long> {

}
