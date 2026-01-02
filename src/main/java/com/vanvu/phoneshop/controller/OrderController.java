package com.vanvu.phoneshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.dto.CartItemDTO;
import com.vanvu.phoneshop.model.Order;
import com.vanvu.phoneshop.model.OrderDetail;
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.CartService;
import com.vanvu.phoneshop.service.OrderService;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    // Trang thanh toán (checkout)
    @GetMapping("/user/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        List<CartItemDTO> cartItems = cartService.getCartItems(user.getUserID());
        if (cartItems.isEmpty()) {
            return "redirect:/user/cart";
        }

        double totalAmount = cartService.getTotalAmount(user.getUserID());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("user", user);

        return "shop/checkout";
    }

    // Xử lý đặt hàng
    @PostMapping("/user/checkout")
    public String placeOrder(HttpSession session, RedirectAttributes redirectAttributes,
                             @RequestParam("shippingAddress") String shippingAddress) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        try {
            Order order = orderService.createOrderFromCart(user.getUserID(), shippingAddress);
            // Cập nhật totalItems trong session
            session.setAttribute("totalItems", 0);
            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công!");
            return "redirect:/user/order/" + order.getOrderID();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đặt hàng thất bại: " + e.getMessage());
            return "redirect:/user/checkout";
        }
    }

    // Xem chi tiết đơn hàng
    @GetMapping("/user/order/{id}")
    public String orderDetail(@PathVariable("id") Integer orderID, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        Order order = orderService.getOrderById(orderID);
        if (order == null || !order.getUser().getUserID().equals(user.getUserID())) {
            return "redirect:/user/orders";
        }

        List<OrderDetail> orderDetails = orderService.getOrderDetails(orderID);
        double totalAmount = orderService.calculateOrderTotal(orderID);

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("totalAmount", totalAmount);

        return "shop/order-detail";
    }

    // Danh sách đơn hàng của user
    @GetMapping("/user/orders")
    public String orderList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        List<Order> orders = orderService.getOrdersByUser(user.getUserID());
        model.addAttribute("orders", orders);

        return "shop/orders";
    }

    // Thanh toán đơn hàng
    @PostMapping("/user/order/{id}/pay")
    public String payOrder(@PathVariable("id") Integer orderID, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        Order order = orderService.getOrderById(orderID);
        if (order == null || !order.getUser().getUserID().equals(user.getUserID())) {
            return "redirect:/user/orders";
        }

        orderService.payOrder(orderID);
        redirectAttributes.addFlashAttribute("success", "Thanh toán thành công! Đơn hàng đang chờ xác nhận.");
        return "redirect:/user/order/" + orderID;
    }

    // Hủy đơn hàng
    @PostMapping("/user/order/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Integer orderID, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        Order order = orderService.getOrderById(orderID);
        if (order == null || !order.getUser().getUserID().equals(user.getUserID())) {
            return "redirect:/user/orders";
        }

        if (order.getStatus() >= 2) {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy đơn hàng đã được xử lý.");
            return "redirect:/user/order/" + orderID;
        }

        orderService.deleteOrder(orderID);
        redirectAttributes.addFlashAttribute("success", "Đã xóa đơn hàng.");
        return "redirect:/user/orders";
    }
}
