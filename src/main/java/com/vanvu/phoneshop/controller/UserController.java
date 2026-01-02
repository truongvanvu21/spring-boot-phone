package com.vanvu.phoneshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.service.UserService;
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.util.MD5Util;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    // Hiển thị trang thông tin cá nhân
    @GetMapping("/user/profile")
    public String showProfilePage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }
        
        // Lấy thông tin user mới nhất từ database
        User user = userService.getUserByEmail(loggedInUser.getEmail());
        model.addAttribute("user", user);
        return "shop/profile";
    }

    // Xử lý cập nhật thông tin cá nhân
    @PostMapping("/user/profile/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                               @RequestParam("phoneNumber") String phoneNumber,
                               @RequestParam(value = "address", required = false) String address,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        // Cập nhật thông tin user
        loggedInUser.setFullName(fullName);
        loggedInUser.setPhoneNumber(phoneNumber);
        loggedInUser.setAddress(address != null ? address : "");

        try {
            User updatedUser = userService.updateUser(loggedInUser);
            if (updatedUser != null) {
                // Cập nhật session với thông tin mới
                session.setAttribute("loggedInUser", updatedUser);
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật. Vui lòng thử lại!");
        }
        
        return "redirect:/user/profile";
    }

    // Hiển thị trang đổi mật khẩu
    @GetMapping("/user/change-password")
    public String showChangePasswordPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }
        User user = userService.getUserByEmail(loggedInUser.getEmail());
        model.addAttribute("user", user);
        return "shop/profile";
    }

    // Xử lý đổi mật khẩu
    @PostMapping("/user/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        // Kiểm tra mật khẩu hiện tại
        String currentPassMD5 = MD5Util.md5String(currentPassword);
        if (!loggedInUser.getPassword().equals(currentPassMD5)) {
            redirectAttributes.addFlashAttribute("errorPass", "Mật khẩu hiện tại không đúng!");
            return "redirect:/user/change-password";
        }

        // Kiểm tra xác nhận mật khẩu mới
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorPass", "Mật khẩu xác nhận không khớp!");
            return "redirect:/user/change-password";
        }

        // Kiểm tra mật khẩu mới không được trùng với mật khẩu cũ
        if (currentPassword.equals(newPassword)) {
            redirectAttributes.addFlashAttribute("errorPass", "Mật khẩu mới không được trùng với mật khẩu cũ!");
            return "redirect:/user/change-password";
        }

        try {
            // Cập nhật mật khẩu mới
            String newPassMD5 = MD5Util.md5String(newPassword);
            loggedInUser.setPassword(newPassMD5);
            User updatedUser = userService.saveUser(loggedInUser);
            
            // Cập nhật session
            session.setAttribute("loggedInUser", updatedUser);
            redirectAttributes.addFlashAttribute("successPass", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorPass", "Đã xảy ra lỗi khi đổi mật khẩu. Vui lòng thử lại!");
        }
        
        return "redirect:/user/change-password";
    }
}
