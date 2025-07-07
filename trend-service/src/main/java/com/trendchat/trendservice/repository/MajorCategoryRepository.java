package com.trendchat.trendservice.repository;

import com.trendchat.trendservice.entity.MajorCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorCategoryRepository extends JpaRepository<MajorCategory, Long> {

    boolean existsByName(String name);

    Optional<MajorCategory> findByName(String name);
}
