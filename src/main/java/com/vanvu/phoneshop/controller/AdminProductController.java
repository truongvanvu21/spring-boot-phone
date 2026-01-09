package com.vanvu.phoneshop.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.model.Category;
import com.vanvu.phoneshop.model.Product;
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.CategoryService;
import com.vanvu.phoneshop.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // Kiểm tra quyền admin
    private boolean isAdmin(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        return loggedInUser != null && loggedInUser.getRole() == 1;
    }

    // Hiển thị danh sách sản phẩm
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String categoryID,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Product> productPage = productService.searchProducts(keyword, categoryID, pageable);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryID", categoryID);
        model.addAttribute("categories", categories);

        return "admin/products";
    }

    // Hiển thị form thêm sản phẩm
    @GetMapping("/products/add")
    public String showAddProductForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", false);

        return "admin/product-form";
    }

    // Xử lý thêm sản phẩm mới
    @PostMapping("/products/add")
    public String addProduct(
            @ModelAttribute Product product,
            @RequestParam("categoryId") String categoryId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            // Kiểm tra mã sản phẩm đã tồn tại
            if (productService.existsById(product.getProductID())) {
                redirectAttributes.addFlashAttribute("error", "Mã sản phẩm đã tồn tại!");
                return "redirect:/admin/products/add";
            }

            // Xử lý upload ảnh
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = productService.uploadImage(imageFile);
                product.setBaseImage(filename);
            }

            // Set category
            Category category = new Category();
            category.setCategoryID(categoryId);
            product.setCategory(category);

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
            return "redirect:/admin/products/add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/products/add";
        }

        return "redirect:/admin/products";
    }

    // Hiển thị form sửa sản phẩm
    @GetMapping("/products/edit/{productID}")
    public String showEditProductForm(@PathVariable String productID, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Product product = productService.getProductById(productID);
        if (product == null) {
            return "redirect:/admin/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", true);

        return "admin/product-form";
    }

    // Xử lý cập nhật sản phẩm
    @PostMapping("/products/edit/{productID}")
    public String updateProduct(
            @PathVariable String productID,
            @ModelAttribute Product product,
            @RequestParam("categoryId") String categoryId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            Product existingProduct = productService.getProductById(productID);
            if (existingProduct == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
                return "redirect:/admin/products";
            }

            // Xử lý upload ảnh mới
            if (imageFile != null && !imageFile.isEmpty()) {
                // Xóa ảnh cũ
                if (existingProduct.getBaseImage() != null) {
                    productService.deleteImage(existingProduct.getBaseImage());
                }
                String filename = productService.uploadImage(imageFile);
                product.setBaseImage(filename);
            } else {
                // Giữ ảnh cũ
                product.setBaseImage(existingProduct.getBaseImage());
            }

            // Set category
            Category category = new Category();
            category.setCategoryID(categoryId);
            product.setCategory(category);

            // Giữ ngày tạo cũ
            product.setCreatedDate(existingProduct.getCreatedDate());
            product.setProductID(productID);

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
            return "redirect:/admin/products/edit/" + productID;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/products/edit/" + productID;
        }

        return "redirect:/admin/products";
    }

    // Xóa sản phẩm
    @PostMapping("/products/delete/{productID}")
    public String deleteProduct(@PathVariable String productID, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            productService.deleteProduct(productID);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }
}
