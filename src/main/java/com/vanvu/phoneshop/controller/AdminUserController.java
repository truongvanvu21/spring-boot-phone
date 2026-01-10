package com.vanvu.phoneshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // Kiểm tra quyền admin
    private boolean isAdmin(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        return loggedInUser != null && loggedInUser.getRole() == 1;
    }

    // Hiển thị danh sách khách hàng
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        // Lấy danh sách khách hàng (role = 0) với phân trang và tìm kiếm
        Page<User> userPage = userService.searchCustomers(keyword, page, size);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        model.addAttribute("size", size);

        return "admin/users";
    }

    // Xem chi tiết khách hàng
    @GetMapping("/users/detail/{userID}")
    public String userDetail(@PathVariable Integer userID, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        User user = userService.getUserById(userID);
        if (user == null || user.getRole() != 0) {
            return "redirect:/admin/users";
        }

        model.addAttribute("user", user);
        return "admin/user-detail";
    }

    // Cập nhật thông tin khách hàng
    @PostMapping("/users/update/{userID}")
    public String updateUser(
            @PathVariable Integer userID,
            @RequestParam String fullName,
            @RequestParam String phoneNumber,
            @RequestParam String address,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            User user = userService.getUserById(userID);
            if (user == null || user.getRole() != 0) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng!");
                return "redirect:/admin/users";
            }

            user.setFullName(fullName);
            user.setPhoneNumber(phoneNumber);
            user.setAddress(address);
            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin khách hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/users/detail/" + userID;
    }
}
