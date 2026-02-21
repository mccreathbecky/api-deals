package com.demo.api_deals.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.demo.api_deals.model.DealsError;
import com.demo.api_deals.service.DealsService;
import com.demo.contract_api_deals.interfaces.DealsApi;
import com.demo.contract_api_deals.models.ActiveDealsResponse;
import com.demo.contract_api_deals.models.ErrorResponse;
import com.demo.contract_api_deals.models.PeakDealsResponse;

import reactor.core.publisher.Mono;

@RestController
public class DealsApiController implements DealsApi {

    @Autowired
    private DealsService dealsService;

    // Used to validate input timeOfDay format
    private static final DateTimeFormatter UPSTREAM_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    

    // TODO: Implement API Key validation
    @Override
    public Mono<ResponseEntity<ActiveDealsResponse>> getActiveDeals(String timeOfDay, String xTrackingId, ServerWebExchange exchange) {

        // Validate timeOfDay format and return appropriate error response if invalid
        LocalTime parsedTimeOfDay = parseTimeOfDay(timeOfDay);

        return dealsService.getActiveDeals(parsedTimeOfDay)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(this::handleError);
    }

    /**
     * Parses the timeOfDay string into a LocalTime object. If the format is invalid, throws a DealsError with details.
     * @param timeOfDay - the input timeOfDay string to parse. Expected format is "HH:mm".
     * @return LocalTime object representing the parsed timeOfDay
     * @throws DealsError if the input timeOfDay format is invalid, containing an appropriate error message, error code, and HTTP status for the response
     */
    private LocalTime parseTimeOfDay(String timeOfDay) {
        try {
            return LocalTime.parse(timeOfDay, UPSTREAM_TIME_FORMATTER);
        } catch (DateTimeParseException e) {

            throw DealsError.builder()
                .message("Invalid format for queryParameter: timeOfDay. Expected HH:mm e.g. 14:30")
                .errorCode("BAD_REQUEST")
                .throwable(e)
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();
        }
    }

    // TODO: Implement API Key validation
    @Override
    public Mono<ResponseEntity<PeakDealsResponse>> getPeakDeals(String xTrackingId, ServerWebExchange exchange) {

        return dealsService.getPeakDeals()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(this::handleError);
    }


    private Mono<ResponseEntity<ErrorResponse>> handleError(Throwable throwable) {
        if (throwable instanceof DealsError) {
            DealsError dealsError = (DealsError) throwable;
            ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(dealsError.getErrorCode())
                .errorMessage(dealsError.getMessage())
                .build();
            return Mono.just(ResponseEntity.status(dealsError.getHttpStatus()).body(errorResponse));
        } else {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("Internal Server Error")
                .errorMessage("An unexpected error occurred: " + throwable.getMessage())
                .build();
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        }
    }
}
