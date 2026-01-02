package com.vanvu.phoneshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Sort;

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


    // Trang chủ - hiển thị cả product và category với lọc và sắp xếp
    @GetMapping("/user/home")
    public String showHomePage(
        @RequestParam(name="cid", required=false) String cid, 
        @RequestParam(name="keyword", required=false) String keyword,
        @RequestParam(name="sort", required=false, defaultValue="default") String sortType,
        Model model) {

        List<Category> categories = categoryService.getAllCategories();
        List<Product> products;

        // Kiểu sắp xếp
        Sort sort = Sort.unsorted();
        if ("price-asc".equals(sortType)) {
            sort = Sort.by("price").ascending();
        } else if ("price-desc".equals(sortType)) {
            sort = Sort.by("price").descending();
        } else if ("name-asc".equals(sortType)) {
            sort = Sort.by("productName").ascending();
        } else if ("name-desc".equals(sortType)) {
            sort = Sort.by("productName").descending();
        } else if ("newest".equals(sortType)) {
            sort = Sort.by("createdDate").descending();
        }

        // Lọc sản phẩm
        if (keyword != null && !keyword.isEmpty()) {
            products = productService.searchProductsByName(keyword, sort);
        } else if (cid != null && !cid.isEmpty()) {
            products = productService.getProductsByCategoryID(cid, sort);
        } else {
            products = productService.getAllProducts(sort);
        }
        
        model.addAttribute("listProduct", products);
        model.addAttribute("listCategory", categories);
        model.addAttribute("currentSort", sortType);
        model.addAttribute("currentCid", cid);
        model.addAttribute("currentKeyword", keyword);
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
