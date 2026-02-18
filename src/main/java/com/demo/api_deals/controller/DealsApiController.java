package com.demo.api_deals.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.demo.api_deals.service.DealsService;
import com.demo.contract_api_deals.interfaces.DealsApi;
import com.demo.contract_api_deals.models.ActiveDealsResponse;
import com.demo.contract_api_deals.models.PeakDealsResponse;

import reactor.core.publisher.Mono;

@RestController
public class DealsApiController implements DealsApi {

    @Autowired
    private DealsService dealsService;

    @Override
    public Mono<ResponseEntity<ActiveDealsResponse>> getActiveDeals(String timeOfDay, String xTrackingId, ServerWebExchange exchange) {

        return dealsService.getActiveDeals(timeOfDay)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<PeakDealsResponse>> getPeakDeals(String xTrackingId, ServerWebExchange exchange) {

        return dealsService.getPeakDeals()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
