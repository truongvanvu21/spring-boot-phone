package com.vanvu.phoneshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // Hiển thị trang đăng nhập
    @GetMapping("/user/login")
    public String showLoginPage() {
        return "shop/login";
    }

    // Xử lý đăng nhập
    @PostMapping("/user/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        User user = userService.login(email, password);
        
        if (user != null) {
            // Lưu thông tin user vào session
            session.setAttribute("loggedInUser", user);
            
            // Kiểm tra role để redirect (role = 1 là admin, role = 0 là user)
            if (user.getRole() == 1) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/home";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng!");
            return "redirect:/user/login";
        }
    }

    // Đăng xuất
    @GetMapping("/user/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đã đăng xuất thành công!");
        return "redirect:/user/login";
    }

    // Hiển thị trang đăng ký
    @GetMapping("/user/register")
    public String showRegisterPage() {
        return "shop/register";
    }

    // Xử lý đăng ký
    @PostMapping("/user/register")
    public String register(@RequestParam("fullName") String fullName,
                          @RequestParam("email") String email,
                          @RequestParam("phoneNumber") String phoneNumber,
                          @RequestParam(value = "address", required = false) String address,
                          @RequestParam("password") String password,
                          @RequestParam("confirmPassword") String confirmPassword,
                          RedirectAttributes redirectAttributes) {
        
        // Kiểm tra mật khẩu xác nhận
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/user/register";
        }

        // Kiểm tra email đã tồn tại
        if (userService.isEmailExists(email)) {
            redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng!");
            return "redirect:/user/register";
        }

        // Tạo user mới
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setAddress(address != null ? address : "");
        newUser.setPassword(password);
        newUser.setRole(0); // Role 0 = user thường
        newUser.setRegisteredDate(java.time.LocalDate.now().toString());

        try {
            userService.saveUser(newUser);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/user/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi đăng ký. Vui lòng thử lại!");
            return "redirect:/user/register";
        }
    }
}
