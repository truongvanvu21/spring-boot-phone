package com.vanvu.phoneshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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
    @Column(name = "CategoryID")
    private String categoryID;
}
