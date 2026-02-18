package com.demo.api_deals.service;

import com.demo.contract_api_deals.models.ActiveDealsResponse;
import com.demo.contract_api_deals.models.PeakDealsResponse;

import reactor.core.publisher.Mono;

public interface DealsService {
    Mono<ActiveDealsResponse> getActiveDeals(String timeOfDay);

    Mono<PeakDealsResponse> getPeakDeals();
}
