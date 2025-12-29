package com.vanvu.phoneshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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
}
