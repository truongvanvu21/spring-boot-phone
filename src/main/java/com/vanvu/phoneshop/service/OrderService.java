package com.vanvu.phoneshop.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vanvu.phoneshop.dto.CartItemDTO;
import com.vanvu.phoneshop.dto.MonthlyRevenueDTO;
import com.vanvu.phoneshop.dto.TopBrandDTO;
import com.vanvu.phoneshop.dto.TopSellingProductDTO;
import com.vanvu.phoneshop.model.Order;
import com.vanvu.phoneshop.model.OrderDetail;
import com.vanvu.phoneshop.model.Product;
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.repository.OrderDetailRepository;
import com.vanvu.phoneshop.repository.OrderRepository;
import com.vanvu.phoneshop.repository.ProductRepository;
import com.vanvu.phoneshop.repository.UserRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    // Đếm số đơn hàng theo trạng thái
    public long countOrdersByStatus(Integer status) {
        return orderRepository.countByStatus(status);
    }

    // Lấy doanh thu tháng hiện tại
    public double getMonthlyRevenue() {
        Double revenue = orderRepository.getMonthlyRevenue();
        return revenue != null ? revenue : 0;
    }

    // Lấy đơn hàng theo trạng thái
    public List<Order> getOrdersByStatus(Integer status) {
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    // Tìm kiếm đơn hàng theo trạng thái và keyword
    public List<Order> searchOrdersByStatusAndKeyword(Integer status, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return orderRepository.findByStatusOrderByOrderDateDesc(status);
        }
        return orderRepository.searchByStatusAndKeyword(status, keyword.trim());
    }

    // Tìm kiếm đơn hàng với phân trang và lọc theo ngày
    public Page<Order> searchOrdersWithFilters(Integer status, String keyword, 
            LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return orderRepository.searchOrdersWithFilters(status, searchKeyword, fromDate, toDate, pageable);
    }

    // Đếm số đơn hàng với bộ lọc
    public long countOrdersWithFilters(Integer status, String keyword, 
            LocalDateTime fromDate, LocalDateTime toDate) {
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return orderRepository.countOrdersWithFilters(status, searchKeyword, fromDate, toDate);
    }

    // Tạo đơn hàng từ giỏ hàng
    @Transactional
    public Order createOrderFromCart(Integer userID, String shippingAddress) {
        User user = userRepository.findById(userID).orElse(null);
        if (user == null) {
            throw new RuntimeException("User không tồn tại");
        }

        List<CartItemDTO> cartItems = cartService.getCartItems(userID);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        // Tạo Order mới
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(0); // Chưa thanh toán
        order.setShippingAddress(shippingAddress);
        order = orderRepository.save(order);

        // Tạo OrderDetails
        for (CartItemDTO item : cartItems) {
            Product product = productRepository.findById(item.getProductID()).orElse(null);
            if (product != null) {
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProduct(product);
                detail.setQuantity(item.getQuantity());
                orderDetailRepository.save(detail);
            }
        }

        // Xóa giỏ hàng sau khi đặt hàng
        cartService.clearCart(userID);

        return order;
    }

    // Lấy đơn hàng theo ID (kèm chi tiết đơn hàng bằng JOIN FETCH)
    public Order getOrderById(Integer orderID) {
        return orderRepository.findByIdWithDetails(orderID);
    }

    // Lấy danh sách đơn hàng của user
    public List<Order> getOrdersByUser(Integer userID) {
        return orderRepository.findByUserUserIDOrderByOrderDateDesc(userID);
    }

    // Lấy chi tiết đơn hàng
    public List<OrderDetail> getOrderDetails(Integer orderID) {
        return orderDetailRepository.findByOrderOrderID(orderID);
    }

    // Cập nhật trạng thái đơn hàng
    public void updateOrderStatus(Integer orderID, Integer status) {
        Order order = orderRepository.findById(orderID).orElse(null);
        if (order != null) {
            order.setStatus(status);
            orderRepository.save(order);
        }
    }

    // Thanh toán đơn hàng (chuyển trạng thái sang đã thanh toán)
    public void payOrder(Integer orderID) {
        updateOrderStatus(orderID, 1);
    }

    // Xác nhận đơn hàng (admin) - trừ số lượng tồn kho
    @Transactional
    public void confirmOrder(Integer orderID) {
        Order order = orderRepository.findById(orderID).orElse(null);
        if (order != null) {
            // Trừ số lượng tồn kho cho từng sản phẩm trong đơn hàng
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderOrderID(orderID);
            for (OrderDetail detail : orderDetails) {
                Product product = detail.getProduct();
                if (product != null) {
                    int newQuantity = product.getQuantity() - detail.getQuantity();
                    product.setQuantity(Math.max(0, newQuantity)); // Không để âm
                    productRepository.save(product);
                }
            }
            // Cập nhật trạng thái đơn hàng
            order.setStatus(2);
            orderRepository.save(order);
        }
    }

    // Hủy đơn hàng (admin)
    public void cancelOrder(Integer orderID) {
        updateOrderStatus(orderID, 3);
    }

    // Xóa đơn hàng hoàn toàn
    @Transactional
    public void deleteOrder(Integer orderID) {
        // Xóa chi tiết đơn hàng trước
        orderDetailRepository.deleteByOrderOrderID(orderID);
        // Sau đó xóa đơn hàng
        orderRepository.deleteById(orderID);
    }

    // Tính tổng tiền đơn hàng dựa vào orderID
    public double calculateOrderTotal(Integer orderID) {
        List<OrderDetail> details = orderDetailRepository.findByOrderOrderID(orderID);
        double total = 0;
        for (OrderDetail detail : details) {
            total += detail.getSubtotal();
        }
        return total;
    }

    // Lấy tất cả đơn hàng (admin)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Lấy ra user đã mua sản phẩm chưa (cho phép review)
    public boolean hasUserPurchasedProduct(Integer userID, String productID) {
        return orderRepository.hasPurchasedProduct(userID, productID);
    }

    // Lấy doanh thu 12 tháng trong năm
    public List<Double> getMonthlyRevenueList(int year) {
        List<MonthlyRevenueDTO> results = orderRepository.getMonthlyRevenueByYear(year);
        
        // Khởi tạo mảng 12 tháng
        List<Double> monthlyRevenue = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            monthlyRevenue.add(0.0);
        }
        
        for (MonthlyRevenueDTO dto : results) {
            if (dto.getMonth() != null && dto.getMonth() >= 1 && dto.getMonth() <= 12) {
                Double revenue = dto.getRevenue() != null ? dto.getRevenue() : 0.0;
                monthlyRevenue.set(dto.getMonth() - 1, revenue);
            }
        }
        
        return monthlyRevenue;
    }

    // Lấy Top sản phẩm bán chạy
    public List<TopSellingProductDTO> getTopSellingProducts(int limit) {
        List<TopSellingProductDTO> results = orderRepository.getTopSellingProducts();
        if (results.size() > limit) {
            return results.subList(0, limit);
        }
        return results;
    }

    // Lấy Top hãng (Category) bán chạy
    public List<TopBrandDTO> getTopSellingBrands(int limit) {
        List<TopBrandDTO> results = orderRepository.getTopSellingBrands();
        if (results.size() > limit) {
            return results.subList(0, limit);
        }
        return results;
    }
}
