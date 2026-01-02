package com.vanvu.phoneshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "OrderDetails")
@Data
public class OrderDetail {
    @Id
    @Column(name = "OrderDetailID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderDetailID;

    @Column(name = "Quantity")
    private int quantity;

    // Quan hệ Many-to-One với Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID")
    @ToString.Exclude
    private Order order;

    // Quan hệ Many-to-One với Product
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ProductID")
    @ToString.Exclude
    private Product product;

    // Tính thành tiền
    public double getSubtotal() {
        return product != null ? product.getPrice() * quantity : 0;
    }
}
