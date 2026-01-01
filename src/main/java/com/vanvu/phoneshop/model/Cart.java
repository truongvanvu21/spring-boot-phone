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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "Carts")
@Data
public class Cart {
    @Id
    @Column(name = "CartID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartID;
    
    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;
    
    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;

    // Quan hệ One-to-One với User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    @ToString.Exclude
    private User user;

    // Quan hệ One-to-Many với CartItem
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<CartItem> cartItems = new ArrayList<>();
}
