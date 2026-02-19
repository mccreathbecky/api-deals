package com.demo.api_deals.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.demo.api_deals.mapper.ResponseDtoToResponseMapper;
import com.demo.api_deals.model.DealResponseDto;
import com.demo.api_deals.model.DealsError;
import com.demo.api_deals.model.RestaurantResponseDto;
import com.demo.api_deals.resource.DealsResource;
import com.demo.contract_api_deals.models.ActiveDealsResponse;
import com.demo.contract_api_deals.models.Deal;
import com.demo.contract_api_deals.models.PeakDealsResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DealsServiceImpl implements DealsService {

    private final ResponseDtoToResponseMapper responseMapper;

    @Autowired
    private DealsResource dealsResource;

    @Override
    public Mono<ActiveDealsResponse> getActiveDeals(LocalTime timeOfDay) {

        return dealsResource.getAllDeals()
                .map(dealsData -> {
                    List<Deal> activeDealsList = new ArrayList<>(); // Initialize mutable list

                    // 0. For each restaurant,
                    for (RestaurantResponseDto restaurant : dealsData.getRestaurants()) {

                        // Parse times into LocalTime for easier comparison
                        LocalTime restaurantStart = responseMapper.parseRestaurantTime(restaurant.getOpen());
                        LocalTime restaurantEnd = responseMapper.parseRestaurantTime(restaurant.getClose());

                        // 1. Check if the restaurant is open at the given timeOfDay
                        if (isDealValidAtTime(restaurantStart, restaurantEnd, timeOfDay)) {

                            // 2. If open, check if each deal is active at the given timeOfDay (or has null for start/end times in which case it's assumed active)
                                for (DealResponseDto deal : restaurant.getDeals()) {
                                    LocalTime dealStart = responseMapper.parseRestaurantTime(deal.getStart());
                                    LocalTime dealEnd = responseMapper.parseRestaurantTime(deal.getEnd());

                                    if (isDealValidAtTime(dealStart, dealEnd, timeOfDay)) {
                                        // 3. If the deal is active, create a Deal object combining restaurant and deal information
                                        Deal activeDeal = responseMapper.mapActiveDealResponse(restaurant, deal);

                                        // 4. Add the active deal to the list of active deals to return in the response
                                        activeDealsList.add(activeDeal);
                                    }
                                    // Else: don't include the deal since it's not active at the given timeOfDay
                                }
                        }
                        // Else: don't include the restaurant's deals since it's closed at the given timeOfDay
                    }
                    return activeDealsList;
                })
                .map(activeDealsResponse -> ActiveDealsResponse.builder().deals(activeDealsResponse).build())
                .doOnError(this::handleError);
    }

    /**
     * Helper method to determine if a deal is valid at the given timeOfDay based on its start and end times. 
     * If start or end time is null, assume the deal is always valid (open).
     * @param startTime - The start time of the deal/restaurant hours as a LocalTime object (can be null)
     * @param endTime   - The end time of the deal/restaurant hours as a LocalTime object (can be null)
     * @param timeOfDay - The time to check the deal/restaurant hours against, as a LocalTime object
     * @return
     */
    private boolean isDealValidAtTime(LocalTime startTime, LocalTime endTime, LocalTime timeOfDay) {
        if (startTime == null || endTime == null) {
            return true; // Assume open if no hours provided
        }

        return (timeOfDay.equals(startTime) || timeOfDay.isAfter(startTime))
         && timeOfDay.isBefore(endTime);
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
