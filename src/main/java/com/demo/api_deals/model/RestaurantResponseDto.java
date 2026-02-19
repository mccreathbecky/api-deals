package com.demo.api_deals.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponseDto {
    private String objectId;
    private String name;
    private String address1;
    private String suburb;
    private String[] cuisines;
    private String imageLink;
    private String open;
    private String close;
    private DealResponseDto[] deals;
}
