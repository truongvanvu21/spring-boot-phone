package com.vanvu.phoneshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.vanvu.phoneshop.service.CategoryService;
import com.vanvu.phoneshop.model.Category;
import java.util.List;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/category")
    public String listCategory(Model model) {
        List<Category> list = (categoryService).getAllCategories();
        model.addAttribute("listCategory", list);
        return "shop/home";
    }
    
}
