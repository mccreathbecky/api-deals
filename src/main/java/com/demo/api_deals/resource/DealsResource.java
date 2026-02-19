package com.demo.api_deals.resource;

import com.demo.api_deals.model.RestaurauntDealsResponseDto;

import reactor.core.publisher.Mono;

public interface DealsResource {
    Mono<RestaurauntDealsResponseDto> getAllDeals();
}
