package com.vanvu.phoneshop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
}
