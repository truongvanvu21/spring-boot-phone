package com.vanvu.phoneshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.model.Order;
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminOrderConfirmController {

    @Autowired
    private OrderService orderService;

    // Kiểm tra quyền admin
    private boolean isAdmin(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        return loggedInUser != null && loggedInUser.getRole() == 1;
    }

    // Hiển thị danh sách đơn hàng chờ xác nhận
    @GetMapping("/order-confirm")
    public String listPendingOrders(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        // Lấy danh sách đơn hàng chờ xác nhận (status = 1: đã thanh toán, chờ xác nhận)
        List<Order> pendingOrders = orderService.getOrdersByStatus(1);
        
        // Tổng số đơn chờ xác nhận
        long totalPending = orderService.countOrdersByStatus(1);

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1); // Tạm thời set 1 trang

        return "admin/order-confirm";
    }

    // Xác nhận đơn hàng
    @PostMapping("/order-confirm/confirm/{orderID}")
    public String confirmOrder(@PathVariable Integer orderID, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            orderService.confirmOrder(orderID);
            redirectAttributes.addFlashAttribute("success", "Xác nhận đơn hàng #ORD-" + orderID + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/order-confirm";
    }

    // Từ chối/Hủy đơn hàng
    @PostMapping("/order-confirm/cancel/{orderID}")
    public String cancelOrder(@PathVariable Integer orderID, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            orderService.cancelOrder(orderID);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối đơn hàng #ORD-" + orderID + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/order-confirm";
    }

    // Xem chi tiết đơn hàng
    @GetMapping("/order-confirm/detail/{orderID}")
    public String orderDetail(@PathVariable Integer orderID, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Order order = orderService.getOrderById(orderID);
        if (order == null) {
            return "redirect:/admin/order-confirm";
        }

        long totalPending = orderService.countOrdersByStatus(1);

        model.addAttribute("order", order);
        model.addAttribute("totalPending", totalPending);

        return "admin/order-confirm-detail";
    }
}
