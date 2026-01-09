package com.vanvu.phoneshop.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.CategoryService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/brands/";

    // Kiểm tra quyền admin
    private boolean isAdmin(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        return loggedInUser != null && loggedInUser.getRole() == 1;
    }

    // Hiển thị danh sách danh mục
    @GetMapping("/categories")
    public String listCategories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Page<Category> categoryPage = categoryService.searchCategories(keyword, PageRequest.of(page, size));
        
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("keyword", keyword);

        return "admin/categories";
    }

    // Hiển thị form thêm danh mục
    @GetMapping("/categories/add")
    public String showAddCategoryForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);

        return "admin/category-form";
    }

    // Xử lý thêm danh mục mới
    @PostMapping("/categories/add")
    public String addCategory(
            @ModelAttribute Category category,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            // Kiểm tra ID đã tồn tại chưa
            if (categoryService.existsById(category.getCategoryID())) {
                redirectAttributes.addFlashAttribute("error", "Mã danh mục đã tồn tại!");
                return "redirect:/admin/categories/add";
            }

            // Xử lý upload ảnh
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                category.setLogoImage(fileName);
            }

            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    // Hiển thị form sửa danh mục
    @GetMapping("/categories/edit/{id}")
    public String showEditCategoryForm(@PathVariable String id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            return "redirect:/admin/categories";
        }

        model.addAttribute("category", category);
        model.addAttribute("isEdit", true);

        return "admin/category-form";
    }

    // Xử lý cập nhật danh mục
    @PostMapping("/categories/edit/{id}")
    public String updateCategory(
            @PathVariable String id,
            @ModelAttribute Category category,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            Category existingCategory = categoryService.getCategoryById(id);
            if (existingCategory == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục!");
                return "redirect:/admin/categories";
            }

            // Cập nhật thông tin
            existingCategory.setCategoryName(category.getCategoryName());

            // Xử lý upload ảnh mới
            if (imageFile != null && !imageFile.isEmpty()) {
                // Xóa ảnh cũ nếu có
                if (existingCategory.getLogoImage() != null) {
                    deleteImage(existingCategory.getLogoImage());
                }
                String fileName = saveImage(imageFile);
                existingCategory.setLogoImage(fileName);
            }

            categoryService.saveCategory(existingCategory);
            redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    // Xóa danh mục
    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable String id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            Category category = categoryService.getCategoryById(id);
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục!");
                return "redirect:/admin/categories";
            }

            // Kiểm tra xem danh mục có sản phẩm không
            if (category.getProducts() != null && !category.getProducts().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa danh mục đang có sản phẩm!");
                return "redirect:/admin/categories";
            }

            // Xóa ảnh nếu có
            if (category.getLogoImage() != null) {
                deleteImage(category.getLogoImage());
            }

            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    // Lưu ảnh
    private String saveImage(MultipartFile file) throws IOException {
        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file unique
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;

        // Lưu file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    // Xóa ảnh
    private void deleteImage(String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
