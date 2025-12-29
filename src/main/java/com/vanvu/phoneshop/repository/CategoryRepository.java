package com.vanvu.phoneshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vanvu.phoneshop.model.Category;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    
}
