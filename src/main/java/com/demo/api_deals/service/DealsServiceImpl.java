package com.demo.api_deals.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.demo.api_deals.models.DealResponseDto;
import com.demo.api_deals.models.DealsError;
import com.demo.api_deals.models.RestaurantResponseDto;
import com.demo.api_deals.resource.DealsResource;
import com.demo.contract_api_deals.models.ActiveDealsResponse;
import com.demo.contract_api_deals.models.Deal;
import com.demo.contract_api_deals.models.PeakDealsResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DealsServiceImpl implements DealsService {

    // For the JSON restaurant data (12-hour format: "3:00pm", "12:00pm")
    private static final DateTimeFormatter DOWNSTREAM_JSON_FORMATTER = DateTimeFormatter.ofPattern("h:mma");

    // Used to validate input timeOfDay format
    private static final DateTimeFormatter UPSTREAM_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    

    @Autowired
    private DealsResource dealsResource;

    @Override
    public Mono<ActiveDealsResponse> getActiveDeals(LocalTime timeOfDay) {

        return dealsResource.getAllDeals()
                .map(dealsData -> {
                    List<Deal> activeDealsList = Arrays.asList(); // Initialize empty list

                    // 0. For each restaurant,
                    for (RestaurantResponseDto restaurant : dealsData.getRestaurants()) {

                        LocalTime restaurantStart = parseRestaurantTime(restaurant.getOpen());
                        LocalTime restaurantEnd = parseRestaurantTime(restaurant.getClose());

                        // 1. Check if the restaurant is open at the given timeOfDay
                        if (isDealValidAtTime(restaurantStart, restaurantEnd, timeOfDay)) {
                            // 2. If open, check if each deal is active at the given timeOfDay (or has null for start/end times in which case it's assumed active)
                                for (DealResponseDto deal : restaurant.getDeals()) {
                                    LocalTime dealStart = parseRestaurantTime(deal.getStart());
                                    LocalTime dealEnd = parseRestaurantTime(deal.getEnd());

                                    if (isDealValidAtTime(dealStart, dealEnd, timeOfDay)) {
                                        // 3. If the deal is active, create a Deal object combining restaurant and deal information
                                        Deal activeDeal = Deal.builder()
                                            .restaurantObjectId(restaurant.getObjectId())
                                            .restaurantName(restaurant.getName())
                                            .restaurantAddress1(restaurant.getAddress1())
                                            .restaurantSuburb(restaurant.getSuburb())
                                            .restaurantOpen(formatRestaurantTimeForUpstream(restaurantStart))   // TODO: use formatted version converting back to 10:30 from LocalTime
                                            .restaurantClose(formatRestaurantTimeForUpstream(restaurantEnd)) // TODO: use formatted version
                                            .dealObjectId(deal.getObjectId())
                                            .discount(Integer.valueOf(deal.getDiscount()))
                                            .dineIn(Boolean.valueOf(deal.getDineIn()))
                                            .lightning(Boolean.valueOf(deal.getLightning()))
                                            .qtyLeft(Integer.valueOf(deal.getQtyLeft()))
                                            .build();

                                        // 4. Add the active deal to the list of active deals to return in the response
                                        activeDealsList.add(activeDeal);
                                    }
                                }
                        }
                        // Else: don't include the restaurant's deals since it's closed at the given timeOfDay
                    }
                    return activeDealsList;
                })
                .map(activeDealsResponse -> ActiveDealsResponse.builder().deals(activeDealsResponse).build())
                .doOnError(this::handleError);
    }

    // TODO: change assumptions for restaurant vs deal hours if no start/end time provided
    private boolean isDealValidAtTime(LocalTime startTime, LocalTime endTime, LocalTime timeOfDay) {
        if (startTime == null || endTime == null) {
            return false; // Assume closed if no hours provided
        }

        return (timeOfDay.equals(startTime) || timeOfDay.isAfter(startTime))
         && (timeOfDay.equals(endTime) || timeOfDay.isBefore(endTime));
    }

    private LocalTime parseRestaurantTime(String time) {
        if (time == null) {
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

    private String formatRestaurantTimeForUpstream(LocalTime time) {
        if (time == null) {
            return null;
        }
        try {
            return time.format(UPSTREAM_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Log the error and treat as invalid time format, excluding the deal from results (since we can't confirm it's active without a valid time)
            // TODO: improve this logging
            System.err.println("Failed to format restaurant time: " + time + ". Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Mono<PeakDealsResponse> getPeakDeals() {
        // Implement the logic to retrieve peak deals
        // For demonstration, return a static response
        PeakDealsResponse peakDeals = new PeakDealsResponse("10:00", "11:30");
        return Mono.just(peakDeals)
                .doOnError(this::handleError);
    }

    private Throwable handleError(Throwable throwable) {
        return DealsError.builder()
                .message("Failed to retrieve deals data: " + throwable.getMessage())
                .errorCode("Internal Server Error")
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .throwable(throwable)
                .build();
    }
}
