package com.demo.api_deals.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.api_deals.resource.DealsResource;
import com.demo.contract_api_deals.models.ActiveDealsResponse;
import com.demo.contract_api_deals.models.Deal;
import com.demo.contract_api_deals.models.PeakDealsResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DealsServiceImpl implements DealsService {

    @Autowired
    private DealsResource dealsResource;

    @Override
    public Mono<ActiveDealsResponse> getActiveDeals(String timeOfDay) {


        // Implement the logic to retrieve active deals based on the timeOfDay
        // For demonstration, return a static response
        List<Deal> deals = Arrays.asList(
                new Deal(
                    "DEA567C5-F64C-3C03-FF00-E3B24909BE00",
                    "Masala Kitchen",
                    "55 Walsh St",
                    "Lower East",
                    "15:00",
                    "21:00",
                    "DEA567C5-0000-3C03-FF00-E3B24909BE00",
                    50,
                    false,
                    true,
                    5));
        ActiveDealsResponse activeDeals = new ActiveDealsResponse(deals);

        return dealsResource.getDealsData()
                // .map(dealsData -> dealsData.getRestaurants())
                .then(Mono.just(activeDeals));
    }

    @Override
    public Mono<PeakDealsResponse> getPeakDeals() {
        // Implement the logic to retrieve peak deals
        // For demonstration, return a static response
        PeakDealsResponse peakDeals = new PeakDealsResponse("10:00", "11:30");
        return Mono.just(peakDeals);
    }

}
