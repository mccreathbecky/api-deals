package com.demo.api_deals.service;

import java.time.LocalTime;

import com.demo.contract_api_deals.models.ActiveDealsResponse;
import com.demo.contract_api_deals.models.PeakDealsResponse;

import reactor.core.publisher.Mono;

public interface DealsService {
    Mono<ActiveDealsResponse> getActiveDeals(LocalTime timeOfDay);

    Mono<PeakDealsResponse> getPeakDeals();
}
