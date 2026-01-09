package com.vanvu.phoneshop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vanvu.phoneshop.repository.CategoryRepository;
import com.vanvu.phoneshop.model.Category;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;    

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Page<Category> searchCategories(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findAll(pageable);
        }
        return categoryRepository.findByCategoryNameContainingIgnoreCase(keyword, pageable);
    }

    public Category getCategoryById(String id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public boolean existsById(String id) {
        return categoryRepository.existsById(id);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(String id) {
        categoryRepository.deleteById(id);
    }
}

