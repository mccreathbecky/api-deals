package com.demo.api_deals.resource;

import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.demo.api_deals.models.RestaurauntDealsResponseDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class DealsResourceApiImpl implements DealsResource {


    @Override
    public Mono<RestaurauntDealsResponseDto> getDealsData() {
        // Implement the logic to retrieve deals data from an external source or database
        // For demonstration, return a static response

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