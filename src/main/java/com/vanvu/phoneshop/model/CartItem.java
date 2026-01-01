package com.vanvu.phoneshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Data
@Entity
@Table(name = "CartItems")
public class CartItem {
    @Id
    @Column(name = "CartItemID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartItemID;
    
    @Column(name = "Quantity")
    private int quantity;

    // Quan hệ Many-to-One với Cart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CartID")
    @ToString.Exclude
    private Cart cart;

    // Quan hệ Many-to-One với Product
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ProductID")
    @ToString.Exclude
    private Product product;
}
