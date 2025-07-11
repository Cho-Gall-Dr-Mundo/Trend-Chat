package com.trendchat.trendservice.repository;

import com.trendchat.trendservice.entity.SubCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    boolean existsByName(String name);

    List<SubCategory> findByNameIn(List<String> names);

    Optional<SubCategory> findByName(String name);
}
