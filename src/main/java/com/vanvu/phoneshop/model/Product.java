package com.vanvu.phoneshop.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "Products")
@Data
public class Product {
    @Id
    @Column(name = "ProductID")
    private String productID;
    @Column(name = "ProductName")
    private String productName;
    @Column(name = "Description")
    private String description;
    @Column(name = "Price")
    private double price;
    @Column(name = "Quantity")
    private int quantity;
    @Column(name = "BaseImage")
    private String baseImage;
    @Column(name = "Specs")
    private String specs;
    @Column(name = "CreatedDate")
    private String createdDate;

    // Quan hệ Many-to-One với Category
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CategoryID")
    @ToString.Exclude
    private Category category;

    // Quan hệ One-to-Many với CartItem
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<CartItem> cartItems = new ArrayList<>();
    
    public String getCategoryID() {
        return category != null ? category.getCategoryID() : null;
    }
}
