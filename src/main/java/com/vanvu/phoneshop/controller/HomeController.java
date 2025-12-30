package com.vanvu.phoneshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.vanvu.phoneshop.service.ProductService;
import com.vanvu.phoneshop.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

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
        return "redirect:/user/home";
    }


    // Trang chủ - hiển thị cả product và category
    @GetMapping("/user/home")
    public String showHomePage(
        @RequestParam(name="cid", required=false) String cid, 
        @RequestParam(name="keyword", required=false) String keyword,Model model) {

        List<Category> categories = categoryService.getAllCategories();
        List<Product> products;

        if(keyword != null && !keyword.isEmpty()) {
            products = productService.searchProductsByName(keyword);
        } else if (cid != null) {
            products = productService.getProductsByCategoryID(cid);
        } else {
            products = productService.getAllProducts();
        }
        
        model.addAttribute("listProduct", products);
        model.addAttribute("listCategory", categories);
        return "shop/home";
    }

    @GetMapping("/user/product/{id}")
    public String productDetail(@PathVariable("id") String id, Model model) {
        Product product = productService.getProductById(id);
        if (product != null) {
            model.addAttribute("product", product);
            return "shop/product-detail";
        }
        return "redirect:/user/home"; // Nếu không thấy sản phẩm thì quay về trang chủ
    }
}
