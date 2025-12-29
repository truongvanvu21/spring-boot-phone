package com.vanvu.phoneshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.vanvu.phoneshop.service.ProductService;
import com.vanvu.phoneshop.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.vanvu.phoneshop.model.Product;
import com.vanvu.phoneshop.model.Category;
import java.util.List;
import org.springframework.ui.Model;

@Controller
public class HomeController {
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/home";
    }


    // Trang chủ - hiển thị cả product và category
    @GetMapping("/user/home")
    public String home(@RequestParam(name="cid", required=false) Integer cid, Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Product> products;

        if (cid != null) {
            products = productService.getProductsByCategoryID(cid.toString());
        } else {
            products = productService.getAllProducts();
        }
        model.addAttribute("listProduct", products);
        model.addAttribute("listCategory", categories);
        return "shop/home";
    }
    
}
