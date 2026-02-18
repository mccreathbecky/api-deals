package com.demo.api_deals.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurauntDealsResponseDto {
    private RestaurantResponseDto[] restaurants;
}
