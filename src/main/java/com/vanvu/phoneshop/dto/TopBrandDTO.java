package com.vanvu.phoneshop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TopBrandDTO {
    private String categoryID;
    private String categoryName;
    private Long totalSold;

    public TopBrandDTO(String categoryID, String categoryName, Long totalSold) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.totalSold = totalSold;
    }
}
