package com.demo.api_deals.resource;

import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.demo.api_deals.models.RestaurauntDealsResponseDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class DealsResourceMockImpl implements DealsResource {


    @Override
    public Mono<RestaurauntDealsResponseDto> getAllDeals() {
        // TODO: replace with call to API (eventually, database)

        RestaurauntDealsResponseDto dealsData = readStaticFile(); // Replace with actual data retrieval logic
        return Mono.just(dealsData);
    }

    private RestaurauntDealsResponseDto readStaticFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ClassLoader classLoader = RestaurauntDealsResponseDto.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("demo/sample-data.json");
            return (RestaurauntDealsResponseDto) objectMapper.readValue(inputStream, RestaurauntDealsResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read static file", e);
        }
    }
}