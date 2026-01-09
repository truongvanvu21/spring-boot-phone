package com.vanvu.phoneshop.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vanvu.phoneshop.model.Order;
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    // Kiểm tra quyền admin
    private boolean isAdmin(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        return loggedInUser != null && loggedInUser.getRole() == 1;
    }

    // Hiển thị danh sách đơn hàng đã xác nhận
    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        // Chuyển đổi LocalDate sang LocalDateTime
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;

        // Lấy danh sách đơn hàng đã xác nhận (status = 2) với phân trang và bộ lọc
        Page<Order> orderPage = orderService.searchOrdersWithFilters(2, keyword, fromDateTime, toDateTime, page, size);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());
        model.addAttribute("size", size);

        return "admin/orders";
    }

    // Xem chi tiết đơn hàng
    @GetMapping("/orders/detail/{orderID}")
    public String orderDetail(@PathVariable Integer orderID, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Order order = orderService.getOrderById(orderID);
        if (order == null) {
            return "redirect:/admin/orders";
        }

        // Số đơn chờ xác nhận (để hiển thị badge)
        long totalPending = orderService.countOrdersByStatus(1);

        model.addAttribute("order", order);
        model.addAttribute("totalPending", totalPending);

        return "admin/order-detail";
    }
}
