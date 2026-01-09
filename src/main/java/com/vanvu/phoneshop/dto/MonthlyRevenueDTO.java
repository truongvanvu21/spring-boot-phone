package com.vanvu.phoneshop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MonthlyRevenueDTO {
    private Integer month;
    private Double revenue;

    public MonthlyRevenueDTO(Integer month, Double revenue) {
        this.month = month;
        this.revenue = revenue;
    }
}
