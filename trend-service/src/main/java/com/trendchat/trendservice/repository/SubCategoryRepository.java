package com.trendchat.trendservice.repository;

import com.trendchat.trendservice.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    boolean existsByName(String name);
}
