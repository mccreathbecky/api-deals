package com.demo.api_deals.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.Tuple;

import com.demo.api_deals.mapper.ResponseDtoToResponseMapper;
import com.demo.api_deals.model.DealResponseDto;
import com.demo.api_deals.model.DealTime;
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

    @Value("${app.config.peak-deals.window-duration-minutes:60}")
    private final int PEAK_WINDOW_DURATION_MINUTES = 60;
    @Value("${app.config.peak-deals.window-step-minutes:30}")
    private final int PEAK_WINDOW_STEP_MINUTES = 30;

    /**
     * This method retrieves all deals from the DealsResource, filters them based on the provided timeOfDay, and returns a list of active deals in an ActiveDealsResponse object.
     * @param timeOfDay - The time to check the deals against, as a LocalTime object
     * @return A Mono containing an ActiveDealsResponse object with the list of active deals at the given timeOfDay (or empty if nothing returned)
     */
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
                        if (isDealValidAtTime(restaurantStart, restaurantEnd, timeOfDay, true)) {

                            // 2. If open, check if each deal is active at the given timeOfDay (or has null for start/end times in which case it's assumed active)
                                for (DealResponseDto deal : restaurant.getDeals()) {
                                    LocalTime dealStart = responseMapper.parseRestaurantTime(deal.getStart());
                                    LocalTime dealEnd = responseMapper.parseRestaurantTime(deal.getEnd());

                                    if (isDealValidAtTime(dealStart, dealEnd, timeOfDay, true)) {
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
     * @param assumeValidIfNull - A boolean flag indicating whether to assume the deal is valid if either startTime or endTime is null (true means assume valid, false means assume invalid)
     * @return
     */
    private boolean isDealValidAtTime(LocalTime startTime, LocalTime endTime, LocalTime timeOfDay, boolean assumeValidIfNull) {
        if (startTime == null || endTime == null) {
            return assumeValidIfNull;
        }

        return (timeOfDay.equals(startTime) || timeOfDay.isAfter(startTime))
         && timeOfDay.isBefore(endTime);
    }


    /**
     * Retrieves the peak period during which the most deals are available.
     * Assumes that the peak period is defined as a continuous 60-minute window where the highest number of deals are active.
     * 
     */
    @Override
    public Mono<PeakDealsResponse> getPeakDeals() {

        return dealsResource.getAllDeals()
                .map(dealsData -> {
                    List<DealTime> dealTimes = new ArrayList<>(); // List to store the active start and end times for each deal
                    
                    // 1. Iterate through the deals data to create a list of the start/end time of every deal.
                    // Assume if an individual deal doesn't have a start/end that the restaurant's open/close should be used.
                    for (RestaurantResponseDto restaurant : dealsData.getRestaurants()) {
                        LocalTime restaurantStart = responseMapper.parseRestaurantTime(restaurant.getOpen());
                        LocalTime restaurantEnd = responseMapper.parseRestaurantTime(restaurant.getClose());

                        for (DealResponseDto deal : restaurant.getDeals()) {
                            LocalTime dealStart = responseMapper.parseRestaurantTime(deal.getStart());
                            LocalTime dealEnd = responseMapper.parseRestaurantTime(deal.getEnd());

                            // Use deal times if provided, otherwise fall back to restaurant hours
                            LocalTime activeStart = (dealStart != null) ? dealStart : restaurantStart;
                            LocalTime activeEnd = (dealEnd != null) ? dealEnd : restaurantEnd;

                            // Store the active start and end times for this deal in a list for processing in the sliding window algorithm
                            dealTimes.add(
                                DealTime.builder()
                                .dealUUID(deal.getObjectId())
                                .startTime(activeStart)
                                .endTime(activeEnd)
                                .build());
                        };
                    };

                    // 2. Use a sliding window algorithm to determine the 60 minute window with the most deals available.
                    PeakDealsResponse peakDeals = findPeakDealsWindow(dealTimes, PEAK_WINDOW_DURATION_MINUTES, PEAK_WINDOW_STEP_MINUTES);

                    return peakDeals;
                })
                .doOnError(this::handleError);
    }

    /**
     * Helper method to find the peak deals window using a sliding window algorithm.
     * @param dealTimes - a list of DealTime objects representing the active start and end times for each deal
     * @param windowDurationMinutes - the duration of the window to evaluate in minutes (e.g. 60 for a 60-minute window)
     * @param windowStepMinutes - the step size in minutes to move the window for each evaluation (e.g. 30 to evaluate every 30 minutes)
     * @return  A PeakDealsResponse object containing the start and end time of the peak window with the most active deals
     */
    private PeakDealsResponse findPeakDealsWindow(List<DealTime> dealTimes, int windowDurationMinutes, int windowStepMinutes) {
        if (dealTimes.isEmpty()) {
            return responseMapper.mapPeakDealsResponse(null, null);
        }

        int maxActiveDeals = 0;
        int currentActiveDeals = 0;
        final LocalTime[] peakWindowStart = new LocalTime[1];
        final LocalTime[] peakWindowEnd = new LocalTime[1];
        
        // Sort deals by start time to optimize the sliding window algorithm (optional but can improve efficiency)
        dealTimes.sort((d1, d2) -> d1.getStartTime().compareTo(d2.getStartTime()));


        // Find the time range we need to search
        LocalTime earliestStart = dealTimes.get(0).getStartTime();
        
        LocalTime latestEnd = dealTimes.stream()
                .map(DealTime::getEndTime)
                .max(LocalTime::compareTo)
                .orElse(LocalTime.MAX);







        // Data structure will roughly look like:
            // [{dealTime:08:30, dealCount:3}, dealTime:09:00, dealCount:5}, {dealTime:09:30, dealCount:2}, ...]
        List<Tuple<LocalTime, Integer>> windowCount = new ArrayList<>();

        // There is a fixed amount of time intervals to search for, so it will be more efficient to iterate only once through each deal and add it to the appropriate windows it falls into
        for (DealTime deal : dealTimes) {
            for (LocalTime windowStart = deal.getStartTime();
                windowStart.isBefore(deal.getEndTime()); 
                windowStart = windowStart.plusMinutes(windowStepMinutes)) {

                final LocalTime currentWindowStart = windowStart;  // Create a final copy for use in lambda

                // Check if we already have a count for this window start time
                Tuple<LocalTime, Integer> existingWindow = windowCount.stream()
                        .filter(window -> window._1().equals(currentWindowStart))
                        .findFirst()
                        .orElse(null);

                if (existingWindow != null) {
                    // If we already have a count for this window, increment it
                    windowCount.set(windowCount.indexOf(existingWindow), new Tuple<>(windowStart, existingWindow._2() + 1));
                } else {
                    // Otherwise, add a new entry for this window with a count of 1
                    windowCount.add(new Tuple<>(windowStart, 1));
                }
            }
        }

        // After processing all deals, find the window with the maximum count of active deals
        windowCount.stream()
            .max((w1, w2) -> w1._2().compareTo(w2._2()))
            .ifPresent(maxWindow -> {
                peakWindowStart[0] = maxWindow._1();
                peakWindowEnd[0] = peakWindowStart[0].plusMinutes(windowDurationMinutes);
            });

        // for (LocalTime i = dealTimes.get(0).getStartTime(); 
        //     i.isBefore(LocalTime.MAX.minusMinutes(windowStepMinutes));  // TODO: check if this correctly handles the last window
        //     i = i.plusMinutes(windowStepMinutes)) {

        //     LocalTime windowStart = i;
        //     LocalTime windowEnd = windowStart.plusMinutes(windowDurationMinutes);

        //     // Count how many deals are active within this window
        //     for (DealTime deal : dealTimes) {
        //         if (isDealValidAtTime(deal.getStartTime(), deal.getEndTime(), windowStart, false)) {
        //             currentActiveDeals++;
        //         }
        //     }

        //     // Update max active deals and corresponding window if this window has more active deals
        //     if (currentActiveDeals > maxActiveDeals) {
        //         maxActiveDeals = currentActiveDeals;
        //         peakWindowStart = windowStart;
        //         peakWindowEnd = windowEnd;
        //     }

        //     // Reset count for the next window
        //     currentActiveDeals = 0;
        // }


        return responseMapper.mapPeakDealsResponse(peakWindowStart[0], peakWindowEnd[0]);
    }

    /**
     * Helper method to handle errors and create a consistent error response structure.
     * @param throwable - The exception that occurred during processing
     * @return A DealsError object containing error details to be returned in the response
     */
    private Throwable handleError(Throwable throwable) {
        return DealsError.builder()
                .message("Failed to retrieve deals data: " + throwable.getMessage())
                .errorCode("Internal Server Error")
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .throwable(throwable)
                .build();
    }
}
