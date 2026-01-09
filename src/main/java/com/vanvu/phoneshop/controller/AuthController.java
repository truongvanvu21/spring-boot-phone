package com.vanvu.phoneshop.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.UserService;
import com.vanvu.phoneshop.util.PasswordUtil;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    
    private static final String failedCount = "loginFailedAttempts";
    private static final String CAPTCHA_KEY = "captchaCode";
    
    // Tạo captcha ngẫu nhiên
    private String generateCaptcha() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder captcha = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            captcha.append(chars.charAt(random.nextInt(chars.length())));
        }
        return captcha.toString();
    }

    // Hiển thị trang đăng nhập
    @GetMapping("/user/login")
    public String showLoginPage(HttpSession session, Model model) {
        Integer failedAttempts = (Integer) session.getAttribute(failedCount);
        if (failedAttempts == null) {
            failedAttempts = 0;
        }
        
        // Nếu đã sai quá 3 lần, hiển thị captcha
        if (failedAttempts >= 3) {
            String captcha = generateCaptcha();
            session.setAttribute(CAPTCHA_KEY, captcha);
            model.addAttribute("showCaptcha", true);
            model.addAttribute("captchaCode", captcha);
        }
        
        model.addAttribute("failedAttempts", failedAttempts);
        return "shop/login";
    }

    // Xử lý đăng nhập
    @PostMapping("/user/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        @RequestParam(value = "captcha", required = false) String captchaInput,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        Integer failedAttempts = (Integer) session.getAttribute(failedCount);
        if (failedAttempts == null) {
            failedAttempts = 0;
        }
        
        // Kiểm tra captcha nếu đã sai quá 3 lần
        if (failedAttempts >= 3) {
            String sessionCaptcha = (String) session.getAttribute(CAPTCHA_KEY);
            if (captchaInput == null || sessionCaptcha == null || !captchaInput.equalsIgnoreCase(sessionCaptcha)) {
                redirectAttributes.addFlashAttribute("error", "Mã captcha không đúng!");
                return "redirect:/user/login";
            }
        }
        
        User user = userService.login(email, password);
        
        if (user != null) {
            // Đăng nhập thành công, reset số lần thất bại
            session.removeAttribute(failedCount);
            session.removeAttribute(CAPTCHA_KEY);
            
            // Lưu thông tin user vào session
            session.setAttribute("loggedInUser", user);
            
            // Kiểm tra role để redirect (role = 1 là admin, role = 0 là user)
            if (user.getRole() == 1) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/home";
            }
        } else {
            // Tăng số lần thất bại
            failedAttempts++;
            session.setAttribute(failedCount, failedAttempts);
            
            if (failedAttempts >= 3) {
                redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng! Vui lòng nhập captcha.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng!");
            }
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

        if (userService.isEmailExists(email)) {
            redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng!");
            return "redirect:/user/register";
        }

        String encodedPassword = PasswordUtil.encodePassword(password);

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setAddress(address != null ? address : "");
        newUser.setPassword(encodedPassword);
        newUser.setRole(0); // Role 0 = user thường

        try {
            userService.saveUser(newUser);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/user/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi đăng ký. Vui lòng thử lại!");
            return "redirect:/user/register";
        }
    }


    // ----------------------- ADMIN AUTHENTICATION -----------------------  
    private static final String ADMIN_SECRET_KEY = "ADMIN@123";
    private static final String adminFailedCount = "adminLoginFailedAttempts";
    private static final String ADMIN_CAPTCHA_KEY = "adminCaptchaCode";

    // Hiển thị trang đăng nhập admin
    @GetMapping("/admin/login")
    public String showAdminLoginPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null && loggedInUser.getRole() == 1) {
            return "redirect:/admin/dashboard";
        }
        
        Integer failedAttempts = (Integer) session.getAttribute(adminFailedCount);
        if (failedAttempts == null) {
            failedAttempts = 0;
        }
        
        // Nếu đã sai quá 3 lần, hiển thị captcha
        if (failedAttempts >= 3) {
            String captcha = generateCaptcha();
            session.setAttribute(ADMIN_CAPTCHA_KEY, captcha);
            model.addAttribute("showCaptcha", true);
            model.addAttribute("captchaCode", captcha);
        }
        
        model.addAttribute("failedAttempts", failedAttempts);
        return "admin/login";
    }

    // Xử lý đăng nhập admin
    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam(value = "captcha", required = false) String captchaInput,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        Integer failedAttempts = (Integer) session.getAttribute(adminFailedCount);
        if (failedAttempts == null) {
            failedAttempts = 0;
        }
        
        // Kiểm tra captcha nếu đã sai quá 3 lần
        if (failedAttempts >= 3) {
            String sessionCaptcha = (String) session.getAttribute(ADMIN_CAPTCHA_KEY);
            if (captchaInput == null || sessionCaptcha == null || !captchaInput.equalsIgnoreCase(sessionCaptcha)) {
                redirectAttributes.addFlashAttribute("error", "Mã captcha không đúng!");
                return "redirect:/admin/login";
            }
        }
        
        User user = userService.login(email, password);
        
        if (user != null && user.getRole() == 1) {
            // Đăng nhập admin thành công, reset số lần thất bại
            session.removeAttribute(adminFailedCount);
            session.removeAttribute(ADMIN_CAPTCHA_KEY);
            
            session.setAttribute("loggedInUser", user);
            
            return "redirect:/admin/dashboard";
        } else {
            // Tăng số lần thất bại
            failedAttempts++;
            session.setAttribute(adminFailedCount, failedAttempts);
            
            if (user != null && user.getRole() != 1) {
                redirectAttributes.addFlashAttribute("error", "Tài khoản này không có quyền admin!");
            } else if (failedAttempts >= 3) {
                redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng! Vui lòng nhập captcha.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng!");
            }
            return "redirect:/admin/login";
        }
    }

    // Đăng xuất admin
    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đã đăng xuất thành công!");
        return "redirect:/admin/login";
    }

    // Hiển thị trang đăng ký admin
    @GetMapping("/admin/register")
    public String showAdminRegisterPage(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null && loggedInUser.getRole() == 1) {
            return "redirect:/admin/dashboard";
        }
        return "admin/register";
    }

    // Xử lý đăng ký admin
    @PostMapping("/admin/register")
    public String adminRegister(@RequestParam("secretKey") String secretKey,
                                @RequestParam("fullName") String fullName,
                                @RequestParam("email") String email,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        
        // Kiểm tra mã bí mật admin
        if (!ADMIN_SECRET_KEY.equals(secretKey)) {
            redirectAttributes.addFlashAttribute("error", "Mã bí mật admin không đúng!");
            return "redirect:/admin/register";
        }
        
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/admin/register";
        }

        if (userService.isEmailExists(email)) {
            redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng!");
            return "redirect:/admin/register";
        }

        String encodedPassword = PasswordUtil.encodePassword(password);

        // Tạo admin user mới
        User newAdmin = new User();
        newAdmin.setFullName(fullName);
        newAdmin.setEmail(email);
        newAdmin.setPhoneNumber(phoneNumber);
        newAdmin.setAddress("");
        newAdmin.setPassword(encodedPassword);
        newAdmin.setRole(1); // Role 1 = admin

        try {
            userService.saveUser(newAdmin);
            redirectAttributes.addFlashAttribute("success", "Đăng ký admin thành công! Vui lòng đăng nhập.");
            return "redirect:/admin/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi đăng ký. Vui lòng thử lại!");
            return "redirect:/admin/register";
        }
    }
}
