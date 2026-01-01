package com.vanvu.phoneshop.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "Categories")
@Data
public class Category {
    @Id
    @Column(name = "CategoryID")
    private String categoryID;
    @Column(name = "CategoryName")
    private String categoryName;
    @Column(name = "LogoImage")
    private String logoImage;

    // Quan hệ One-to-Many với Product
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Product> products = new ArrayList<>();
}
