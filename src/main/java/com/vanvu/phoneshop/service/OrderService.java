package com.vanvu.phoneshop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vanvu.phoneshop.dto.CartItemDTO;
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

    // Lấy đơn hàng theo ID
    public Order getOrderById(Integer orderID) {
        return orderRepository.findById(orderID).orElse(null);
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

    // Xác nhận đơn hàng (admin)
    public void confirmOrder(Integer orderID) {
        updateOrderStatus(orderID, 2);
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

    //
    public boolean hasUserPurchasedProduct(Integer userID, String productID) {
        return orderRepository.hasPurchasedProduct(userID, productID);
    }
}
