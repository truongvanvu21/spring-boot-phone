package com.vanvu.phoneshop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TopSellingProductDTO {
    private String productID;
    private String productName;
    private String baseImage;
    private Long soldQuantity;
    private Double totalRevenue;

    public TopSellingProductDTO(String productID, String productName, String baseImage, Long soldQuantity, Double totalRevenue) {
        this.productID = productID;
        this.productName = productName;
        this.baseImage = baseImage;
        this.soldQuantity = soldQuantity;
        this.totalRevenue = totalRevenue;
    }
}
