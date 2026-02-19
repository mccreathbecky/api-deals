package com.demo.api_deals.mapper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;

import com.demo.api_deals.model.DealResponseDto;
import com.demo.api_deals.model.RestaurantResponseDto;
import com.demo.contract_api_deals.models.Deal;

@Service
public class ResponseDtoToResponseMapper {

    // For the JSON restaurant data (12-hour format: "3:00pm", "12:00pm")
    public static final DateTimeFormatter DOWNSTREAM_JSON_FORMATTER = DateTimeFormatter.ofPattern("h:mma");

    // Used to validate input timeOfDay format
    public static final DateTimeFormatter UPSTREAM_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    
    public Deal mapActiveDealResponse(RestaurantResponseDto restaurantDto, DealResponseDto dealDto) {
        return Deal.builder()
            .restaurantObjectId(restaurantDto.getObjectId())
            .restaurantName(restaurantDto.getName())
            .restaurantAddress1(restaurantDto.getAddress1())
            .restaurantSuburb(restaurantDto.getSuburb())
            .restaurantOpen(formatRestaurantTimeForUpstream(restaurantDto.getOpen()))
            .restaurantClose(formatRestaurantTimeForUpstream(restaurantDto.getClose()))
            .dealObjectId(dealDto.getObjectId())
            .discount(Integer.valueOf(dealDto.getDiscount()))
            .dineIn(Boolean.valueOf(dealDto.getDineIn()))
            .lightning(Boolean.valueOf(dealDto.getLightning()))
            .qtyLeft(Integer.valueOf(dealDto.getQtyLeft()))
            .build();
    }

    
    /**
     * Helper method to format restaurant hours into the expected "HH:mm" format for the API response. 
     * If input time is null, returns null. If input time is in an invalid format, logs the error and returns null (treating it as invalid/unknown time).
     * @param time - the string object representing the restaurant hours to format (e.g. 11:30am). Can be null, in which case null is returned.
     * @return a string representing the formatted time in "HH:mm" format (e.g. 11:30), or null if input time is null or invalid
     */
    public String formatRestaurantTimeForUpstream(String time) {
        if (time == null) {
            return null;
        }
        try {
            return LocalTime.parse(time, DOWNSTREAM_JSON_FORMATTER).format(UPSTREAM_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Log the error and treat as invalid time format, excluding the deal from results (since we can't confirm it's active without a valid time)
            // TODO: improve this logging
            System.err.println("Failed to format restaurant time: " + time + ". Error: " + e.getMessage());
            return null;
        }
    }



    /**
     * Helper method to parse restaurant hours from the downstream JSON data, which can be in 12-hour format with am/pm (e.g. "3:00pm", "12:00am").
     * @param time - the input time string to parse, expected in 12-hour format with am/pm (e.g. "3:00pm", "12:00am"). Can be null, in which case null is returned.
     * @return
     */
    public LocalTime parseRestaurantTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(time, DOWNSTREAM_JSON_FORMATTER);
        } catch (DateTimeParseException e) {
            // Log the error and treat as invalid time format, excluding the deal from results (since we can't confirm it's active without a valid time)
            // TODO: improve this logging
            System.err.println("Failed to parse restaurant time: " + time + ". Error: " + e.getMessage());
            return null;
        }
    }
}
