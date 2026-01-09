package com.vanvu.phoneshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vanvu.phoneshop.model.Category;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    Page<Category> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);
}
