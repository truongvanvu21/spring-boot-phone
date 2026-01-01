package com.vanvu.phoneshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private String productID;
    private String productName;
    private String baseImage;
    private double price;
    private int quantity;
    private double subtotal;
    
    // Constructor cho query JOIN (không có subtotal, sẽ tính sau)
    public CartItemDTO(String productID, String productName, String baseImage, double price, int quantity) {
        this.productID = productID;
        this.productName = productName;
        this.baseImage = baseImage;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price * quantity;
    }
}
