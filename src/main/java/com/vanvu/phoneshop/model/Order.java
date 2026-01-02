package com.vanvu.phoneshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "Orders")
@Data
public class Order {
    @Id
    @Column(name = "OrderID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderID;

    @Column(name = "OrderDate")
    private LocalDateTime orderDate;

    @Column(name = "Status")
    private Integer status = 0; // 0: chưa thanh toán, 1: đã thanh toán, 2: xác nhận thành công, 3: từ chối

    @Column(name = "ShippingAddress")
    private String shippingAddress; // Địa chỉ nhận hàng

    // Quan hệ Many-to-One với User
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserID")
    @ToString.Exclude
    private User user;

    // Quan hệ One-to-Many với OrderDetail
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // Lấy tên trạng thái
    public String getStatusName() {
        if (status == null) {
            return "Không xác định";
        }

        if (status == 0) {
            return "Chưa thanh toán";
        } else if (status == 1) {
            return "Đã thanh toán (Chờ xác nhận)";
        } else if (status == 2) {
            return "Xác nhận thành công";
        } else if (status == 3) {
            return "Đã hủy";
        } else {
            return "Không xác định";
        }
    }

    // Lấy class CSS cho badge trạng thái
    public String getStatusBadgeClass() {
        if (status == null) return "bg-secondary";
        
        if (status == 0) {
            return "bg-warning text-dark";
        } else if (status == 1) {
            return "bg-info";
        } else if (status == 2) {
            return "bg-success";
        } else if (status == 3) {
            return "bg-danger";
        } else {
            return "bg-secondary";
        }
    }

    // Tính tổng tiền đơn hàng
    public double getTotalAmount() {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (OrderDetail detail : orderDetails) {
            total += detail.getSubtotal();
        }
        return total;
    }
}
