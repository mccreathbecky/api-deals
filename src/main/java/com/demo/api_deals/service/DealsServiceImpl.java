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
import com.demo.api_deals.model.TimeEvent;
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
                        LocalTime restaurantStart = responseMapper.parseRestaurantDtoTime(restaurant.getOpen());
                        LocalTime restaurantEnd = responseMapper.parseRestaurantDtoTime(restaurant.getClose());

                        // 1. Check if the restaurant is open at the given timeOfDay
                        if (isDealValidAtTime(restaurantStart, restaurantEnd, timeOfDay, true)) {

                            // 2. If open, check if each deal is active at the given timeOfDay (or has null for start/end times in which case it's assumed active)
                                for (DealResponseDto deal : restaurant.getDeals()) {
                                    LocalTime dealStart = responseMapper.parseRestaurantDtoTime(deal.getStart());
                                    LocalTime dealEnd = responseMapper.parseRestaurantDtoTime(deal.getEnd());

                                    // Check if deal is active, and whether there are any deals left
                                    if (isDealValidAtTime(dealStart, dealEnd, timeOfDay, true) 
                                        && (deal.getQtyLeft() != null && Integer.valueOf(deal.getQtyLeft()) > 0)) { 

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
                    if (dealsData == null || dealsData.getRestaurants() == null) {
                        return responseMapper.mapPeakDealsResponse(null, null);
                    }

                    // Create events for all deal start and end times
                    List<TimeEvent> events = new ArrayList<>();

                    // 1. Iterate through the deals data to create a list of the start/end time of every deal.
                    // Assume if an individual deal doesn't have a start/end that the restaurant's open/close should be used.
                    for (RestaurantResponseDto restaurant : dealsData.getRestaurants()) {
                        LocalTime restaurantStart = responseMapper.parseRestaurantDtoTime(restaurant.getOpen());
                        LocalTime restaurantEnd = responseMapper.parseRestaurantDtoTime(restaurant.getClose());

                        if (restaurant.getDeals() == null) {
                            continue; // No deals for this restaurant, skip to the next one
                        }

                        for (DealResponseDto deal : restaurant.getDeals()) {
                            LocalTime dealStart = responseMapper.parseRestaurantDtoTime(deal.getStart());
                            LocalTime dealEnd = responseMapper.parseRestaurantDtoTime(deal.getEnd());

                            // Use deal times if provided, otherwise fall back to restaurant hours
                            LocalTime activeStart = (dealStart != null) ? dealStart : restaurantStart;
                            LocalTime activeEnd = (dealEnd != null) ? dealEnd : restaurantEnd;

                            // Store the active start and end times for this deal in a list for processing in the peak window algoirthm
                            events.add(new TimeEvent(activeStart, TimeEvent.EventType.START));
                            events.add(new TimeEvent(activeEnd, TimeEvent.EventType.END));
                        }
                    };

                    // 2. Determine the peak deal window
                    PeakDealsResponse peakDeals = findPeakDealsWindow(events);

                    return peakDeals;
                })
                .doOnError(this::handleError);
    }

    /**
     * Helper method to find the peak deals window using a sweep line algorithm.
     * @param dealTimes - a list of TimeEvent objects representing the active start and end times for each deal
     * @return  A PeakDealsResponse object containing the start and end time of the peak window with the most active deals
     */
    public PeakDealsResponse findPeakDealsWindow(List<TimeEvent> events) {
        if (events == null || events.isEmpty()) {
            return responseMapper.mapPeakDealsResponse(null, null);
        }

            /*
            Visual Explanation of Sweep Line Algorithm for findPeakDealsWindow:

            Imagine the following example set of deals with their active times (times are in 24-hour format for clarity):
            Masala Kitchen Deal 1: 3pm ██████ 9pm (50% off)
            Masala Kitchen Deal 2: 3pm ██████ 9pm (40% off, no specific times → uses restaurant hours)
            ABC Chicken Deal 1:   12pm ███████████ 11pm
            ABC Chicken Deal 2:   12pm ███████████ 11pm
            Vrindavan Deal 1:      3pm ██████ 9pm
            Kekou Deal 1:          2pm ███████ 9pm
            Kekou Deal 2:          5pm ████ 9pm
            Gyoza Deal 1:          4pm █████ 10pm (no start → uses restaurant)
            Gyoza Deal 2:          4pm █████ 10pm (no end → uses restaurant)
            OzzyThai Deal 1:       8am ███████ 3pm
            OzzyThai Deal 2:       8am ███████ 3pm

            Timeline:
            8am   9am  10am  11am  12pm  1pm  2pm  3pm  4pm  5pm  6pm  7pm  8pm  9pm  10pm  11pm
            2     2    2     2     4     4    5    7    9    10   10   10   10   8    6     4
                                                        ↑─────PEAK = 10 deals─────↑
            
            */


        // Sort events by time, 
        // If equal, sort START events before END events
        events.sort((e1, e2) -> {
            int timeComparison = e1.getTime().compareTo(e2.getTime());
            if (timeComparison != 0) return timeComparison;
            return e1.getEventType() == TimeEvent.EventType.START ? -1 : 1; // START events come before END events if times are equal
        });


        // Initialise sweep line variables to track the number of active deals and the peak window
        int maxActiveDeals = 0;
        int currentActiveDeals = 0;
        LocalTime peakWindowStart = events.get(0).getTime(); // Initialize to the time of the first event
        LocalTime peakWindowEnd = null; // Initialize to the time of the first event


        // Sweep through the events, counting active deals and updating the peak window when we find a new maximum
        for (TimeEvent event : events) {
            if (event.getEventType() == TimeEvent.EventType.START) {
                currentActiveDeals++;
                // Check if this is the new peak
                if (currentActiveDeals > maxActiveDeals) {
                    maxActiveDeals = currentActiveDeals;
                    peakWindowStart = event.getTime();
                    peakWindowEnd = null; // Reset end time until we find the end of this peak window
                }
            } else {
                // END event
                // If we're currently at peak and this is the first END, this is when peak ends
                if (currentActiveDeals == maxActiveDeals && peakWindowEnd == null) {
                    peakWindowEnd = event.getTime();
                }
                currentActiveDeals--;
            }
        }

        // Handle edge case: peak continues until the last deal ends
        if (peakWindowEnd == null) {
            peakWindowEnd = events.get(events.size() - 1).getTime();
        }
    

        return responseMapper.mapPeakDealsResponse(peakWindowStart, peakWindowEnd);
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
