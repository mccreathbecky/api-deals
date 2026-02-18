package com.demo.api_deals.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.demo.contract_api_deals.interfaces.DealsApi;
import com.demo.contract_api_deals.models.ActiveDealsResponse;
import com.demo.contract_api_deals.models.Deal;
import com.demo.contract_api_deals.models.PeakDealsResponse;

import reactor.core.publisher.Mono;

@RestController
public class DealsApiController implements DealsApi {

    @Override
    public Mono<ResponseEntity<ActiveDealsResponse>> getActiveDeals(String timeOfDay, String xTrackingId, ServerWebExchange exchange) {
        // For demonstration, return a static list of deals
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
        return Mono.just(ResponseEntity.ok(activeDeals));
    }

    @Override
    public Mono<ResponseEntity<PeakDealsResponse>> getPeakDeals(String xTrackingId, ServerWebExchange exchange) {

        // For demonstration, return a fixed response
        PeakDealsResponse peakDeals = new PeakDealsResponse("10:00", "11:30");

        return Mono.just(ResponseEntity.ok(peakDeals));
    }
}
