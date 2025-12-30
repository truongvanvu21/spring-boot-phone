package com.vanvu.phoneshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Users")
@Data
public class User {
    @Id
    @Column(name = "UserID")
    private int userID;
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
    @Column(name = "RegisteredDate")
    private String registeredDate;
    @Column(name = "Role")
    private int role;
}
