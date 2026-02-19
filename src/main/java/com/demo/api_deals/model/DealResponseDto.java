package com.demo.api_deals.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealResponseDto {
    private String objectId;
    private String discount;
    private String dineIn;
    private String lightning;
    private String start;
    private String end;
    private String qtyLeft;
}
