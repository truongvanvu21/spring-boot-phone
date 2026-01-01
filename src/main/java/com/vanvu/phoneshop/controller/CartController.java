package com.vanvu.phoneshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.dto.CartItemDTO;
import com.vanvu.phoneshop.service.CartService;
import com.vanvu.phoneshop.model.User;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    // Hiển thị trang giỏ hàng
    @GetMapping("/user/cart")
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        List<CartItemDTO> cartItems = cartService.getCartItems(user.getUserID());
        int totalItems = cartService.getTotalItems(user.getUserID());
        double totalAmount = cartService.getTotalAmount(user.getUserID());

        model.addAttribute("cartItems", cartItems);
        // model.addAttribute("totalItems", totalItems);
        session.setAttribute("totalItems", totalItems);
        model.addAttribute("totalAmount", totalAmount);

        return "shop/cart";
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/user/cart/add/{productID}")
    public String addToCart(@PathVariable("productID") String productID,
                            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        try {
            cartService.addToCart(user.getUserID(), productID, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể thêm sản phẩm: " + e.getMessage());
        }
        
        return "redirect:/user/cart";
    }

    // Cập nhật số lượng sản phẩm
    @PostMapping("/user/cart/update/{productID}")
    public String updateCartItem(@PathVariable("productID") String productID,
                                  @RequestParam("quantity") Integer quantity,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        try {
            cartService.updateCartItem(user.getUserID(), productID, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật số lượng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật: " + e.getMessage());
        }

        return "redirect:/user/cart";
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @GetMapping("/user/cart/remove/{productID}")
    public String removeCartItem(@PathVariable("productID") String productID,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        try {
            cartService.removeCartItem(user.getUserID(), productID);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm: " + e.getMessage());
        }

        return "redirect:/user/cart";
    }

    // Xóa tất cả sản phẩm trong giỏ hàng
    @GetMapping("/user/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        try {
            cartService.clearCart(user.getUserID());
            redirectAttributes.addFlashAttribute("success", "Đã xóa tất cả sản phẩm trong giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa giỏ hàng: " + e.getMessage());
        }

        return "redirect:/user/cart";
    }
}
