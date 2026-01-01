package com.vanvu.phoneshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import lombok.ToString;

@Entity
@Table(name = "Users")
@Data
public class User {
    @Id
    @Column(name = "UserID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userID;
    @Column(name = "FullName")
    private String fullName;
    @Column(name = "Email")
    private String email;
    @Column(name = "Password")
    private String password;
    @Column(name = "PhoneNumber")
    private String phoneNumber;
    @Column(name = "Address")
    private String address;
    @Column(name = "Role")
    private int role;

    // Quan hệ One-to-One với Cart (1 User có 1 Cart)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Cart cart;
}
