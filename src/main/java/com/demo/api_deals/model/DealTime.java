package com.demo.api_deals.model;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealTime{
    private String dealUUID;
    private LocalTime startTime;
    private LocalTime endTime;
}
