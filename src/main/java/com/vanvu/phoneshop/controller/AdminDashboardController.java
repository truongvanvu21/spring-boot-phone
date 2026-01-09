package com.vanvu.phoneshop.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.dto.TopBrandDTO;
import com.vanvu.phoneshop.dto.TopSellingProductDTO;
import com.vanvu.phoneshop.model.Order;
import com.vanvu.phoneshop.model.Product;
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.OrderService;
import com.vanvu.phoneshop.service.ProductService;
import com.vanvu.phoneshop.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // private boolean isAdmin(HttpSession session) {
    //     User loggedInUser = (User) session.getAttribute("loggedInUser");
    //     return loggedInUser != null && loggedInUser.getRole() == 1;
    // }

    // Hiển thị trang Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getRole() != 1) {
            return "redirect:/admin/login";
        }

        // Lấy số liệu thống kê cơ bản
        long totalProducts = productService.countProducts();
        long totalUserCustomer = userService.countUserCustomer();
        long newOrders = orderService.countOrdersByStatus(1); // Đơn chờ xác nhận (đã thanh toán)
        double monthlyRevenue = orderService.getMonthlyRevenue();

        // Lấy danh sách đơn hàng chờ xác nhận
        List<Order> pendingOrders = orderService.getOrdersByStatus(1);

        // Lấy doanh thu 12 tháng của năm hiện tại cho biểu đồ
        int currentYear = LocalDate.now().getYear();
        List<Double> monthlyRevenueList = orderService.getMonthlyRevenueList(currentYear);

        // Lấy Top 10 sản phẩm bán chạy
        List<TopSellingProductDTO> topBestSellers = orderService.getTopSellingProducts(10);
        List<String> bestSellerNames = new ArrayList<>();
        List<Long> bestSellerCounts = new ArrayList<>();
        for (TopSellingProductDTO dto : topBestSellers) {
            bestSellerNames.add(dto.getProductName());
            bestSellerCounts.add(dto.getSoldQuantity());
        }

        // Lấy Top 5 hãng bán chạy
        List<TopBrandDTO> topBrands = orderService.getTopSellingBrands(5);
        List<String> topBrandNames = new ArrayList<>();
        List<Long> topBrandCounts = new ArrayList<>();
        for (TopBrandDTO dto : topBrands) {
            topBrandNames.add(dto.getCategoryName());
            topBrandCounts.add(dto.getTotalSold());
        }
        // Lấy sản phẩm sắp hết hàng (quantity <= 10)
        List<Product> lowStockProducts = productService.getLowStockProducts(20);
        List<String> lowStockNames = new ArrayList<>();
        List<Integer> lowStockCounts = new ArrayList<>();
        for (Product product : lowStockProducts) {
            lowStockNames.add(product.getProductName());
            lowStockCounts.add(product.getQuantity());
        }

        // Đưa dữ liệu vào model
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUserCustomer", totalUserCustomer);
        model.addAttribute("newOrders", newOrders);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("pendingOrders", pendingOrders);

        // Dữ liệu cho biểu đồ
        model.addAttribute("monthlyRevenueList", monthlyRevenueList);
        
        model.addAttribute("bestSellerNames", bestSellerNames);
        model.addAttribute("bestSellerCounts", bestSellerCounts);
        
        model.addAttribute("topBrandNames", topBrandNames);
        model.addAttribute("topBrandCounts", topBrandCounts);
        
        model.addAttribute("lowStockNames", lowStockNames);
        model.addAttribute("lowStockCounts", lowStockCounts);

        return "admin/dashboard";
    }

    // Xác nhận đơn hàng
    @PostMapping("/order/confirm/{orderID}")
    public String confirmOrder(@PathVariable Integer orderID, RedirectAttributes redirectAttributes) {
        try {
            orderService.confirmOrder(orderID);
            redirectAttributes.addFlashAttribute("success", "Xác nhận đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // Hủy đơn hàng
    @PostMapping("/order/cancel/{orderID}")
    public String cancelOrder(@PathVariable Integer orderID, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(orderID);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

}
