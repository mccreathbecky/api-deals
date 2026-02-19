package com.demo.api_deals.controller;

import static org.mockito.ArgumentMatchers.any;

import java.time.Duration;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.demo.api_deals.BaseTestClass;
import com.demo.api_deals.helpers.FileLoader;
import com.demo.api_deals.service.DealsService;
import com.demo.contract_api_deals.models.ActiveDealsResponse;

import reactor.core.publisher.Mono;

@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DealsApiControllerTest extends BaseTestClass {

    @MockitoBean
    private DealsService dealsService;

    @Autowired
    private WebTestClient webTestClient;

    private final FileLoader fileLoader = new FileLoader();

    @BeforeAll
    void setup() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofMillis(30000))
                .build();
    }

    @Test
    void testGetActiveDeals_onValidInput_expectSuccessResponse() {
        // Arrange
        ActiveDealsResponse mockResponse = (ActiveDealsResponse) fileLoader.readFileAsObject("responses/service-success-deals-response.json", ActiveDealsResponse.class);
        String timeOfDay = "14:30";

        Mockito.when(dealsService.getActiveDeals(any(LocalTime.class)))
                .thenReturn(Mono.just(mockResponse)
                .delayElement(Duration.ofMillis(500)));

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/active")
                        .queryParam("timeOfDay", timeOfDay)
                        .build())
                .header("x-api-key", "DUMMY_VALUE")
                .exchange()

        // Assert
                .expectStatus().isEqualTo(200)
                .expectBody(ActiveDealsResponse.class)
                .isEqualTo(mockResponse);

        Mockito.verify(dealsService, Mockito.times(1)).getActiveDeals(any(LocalTime.class));
    }

    @Test
    void testGetActiveDeals_onInvalidQueryParam_expect400BadRequest() {
        // Arrange
        ActiveDealsResponse mockResponse = (ActiveDealsResponse) fileLoader.readFileAsObject("responses/service-success-deals-response.json", ActiveDealsResponse.class);
        String timeOfDay = "abcd";

        Mockito.when(dealsService.getActiveDeals(any(LocalTime.class)))
                .thenReturn(Mono.just(mockResponse)
                .delayElement(Duration.ofMillis(500)));

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/active")
                        .queryParam("timeOfDay", timeOfDay)
                        .build())
                .header("x-api-key", "DUMMY_VALUE")
                .header("x-tracking-id", "myid123")
                .exchange()

        // Assert
                .expectStatus().isEqualTo(400)
                .expectBody()
                .jsonPath("$.errorMessage").isEqualTo("Invalid format for queryParameter: timeOfDay. Expected HH:mm e.g. 14:30")
                .jsonPath("$.errorCode").isEqualTo("BAD_REQUEST")
                .jsonPath("$.trackingId").isEqualTo("myid123");

        Mockito.verify(dealsService, Mockito.times(0)).getActiveDeals(any(LocalTime.class));
    }

    @Test
    void testGetActiveDeals_onMissingQueryParam_expect400BadRequest() {
        // Arrange
        ActiveDealsResponse mockResponse = (ActiveDealsResponse) fileLoader.readFileAsObject("responses/service-success-deals-response.json", ActiveDealsResponse.class);

        Mockito.when(dealsService.getActiveDeals(any(LocalTime.class)))
                .thenReturn(Mono.just(mockResponse)
                .delayElement(Duration.ofMillis(500)));

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/active")
                        // .queryParam("timeOfDay", timeOfDay)
                        .build())
                .header("x-api-key", "DUMMY_VALUE")
                .header("x-tracking-id", "myid123")
                .exchange()

        // Assert
                .expectStatus().isEqualTo(400)
                .expectBody()
                .jsonPath("$.errorMessage").isEqualTo("400 BAD_REQUEST \"Required query parameter 'timeOfDay' is not present.\"")
                .jsonPath("$.errorCode").isEqualTo("BAD_REQUEST")
                .jsonPath("$.trackingId").isEqualTo("myid123");

        Mockito.verify(dealsService, Mockito.times(0)).getActiveDeals(any(LocalTime.class));
    }


    @Test
    void testGetActiveDeals_onNullPointerException_expect500InternalServerError() {
        // Arrange
        ActiveDealsResponse mockResponse = (ActiveDealsResponse) fileLoader.readFileAsObject("responses/service-success-deals-response.json", ActiveDealsResponse.class);
        String timeOfDay = "10:30";

        Mockito.when(dealsService.getActiveDeals(any(LocalTime.class)))
                .thenReturn(Mono.error(new NullPointerException("Simulated null pointer exception")));

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/active")
                        .queryParam("timeOfDay", timeOfDay)
                        .build())
                .header("x-api-key", "DUMMY_VALUE")
                .header("x-tracking-id", "myid123")
                .exchange()

        // Assert
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$.errorMessage").isEqualTo("An unexpected error occurred. Please contact support if the issue persists.")
                .jsonPath("$.errorCode").isEqualTo("INTERNAL_SERVER_ERROR")
                .jsonPath("$.trackingId").isEqualTo("myid123");

        Mockito.verify(dealsService, Mockito.times(1)).getActiveDeals(any(LocalTime.class));
    }

    @Test
    void testGetActiveDeals_onRuntimeException_expect500InternalServerError() {
        // Arrange
        ActiveDealsResponse mockResponse = (ActiveDealsResponse) fileLoader.readFileAsObject("responses/service-success-deals-response.json", ActiveDealsResponse.class);
        String timeOfDay = "10:30";

        Mockito.when(dealsService.getActiveDeals(any(LocalTime.class)))
                .thenReturn(Mono.error(new RuntimeException("Simulated runtime exception")));

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/active")
                        .queryParam("timeOfDay", timeOfDay)
                        .build())
                .header("x-api-key", "DUMMY_VALUE")
                .header("x-tracking-id", "myid123")
                .exchange()

        // Assert
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$.errorMessage").isEqualTo("An unexpected error occurred. Please contact support if the issue persists.")
                .jsonPath("$.errorCode").isEqualTo("INTERNAL_SERVER_ERROR")
                .jsonPath("$.trackingId").isEqualTo("myid123");

        Mockito.verify(dealsService, Mockito.times(1)).getActiveDeals(any(LocalTime.class));
    }

    // @Test
    // void testGetActiveDeals_onMissingApiKey_expectDescriptiveErrorResponse() {
    //     // Arrange
    //     ActiveDealsResponse mockResponse = (ActiveDealsResponse) fileLoader.readFileAsObject("responses/service-success-deals-response.json", ActiveDealsResponse.class);
    //     String timeOfDay = "14:30";

    //     Mockito.when(dealsService.getActiveDeals(any(LocalTime.class)))
    //             .thenReturn(Mono.just(mockResponse)
    //             .delayElement(Duration.ofMillis(500)));

    //     // Act
    //     webTestClient.get()
    //             .uri(uriBuilder -> uriBuilder
    //                     .path("/v1/active")
    //                     .queryParam("timeOfDay", timeOfDay)
    //                     .build())
    //             // .header("x-api-key", "DUMMY_VALUE")
    //             .header("x-tracking-id", "myid123")
    //             .exchange()

    //     // Assert
    //             .expectStatus().isEqualTo(400)
    //             .expectBody()
    //             .jsonPath("$.errorMessage").isEqualTo("400 BAD_REQUEST \"Required query parameter 'timeOfDay' is not present.\"")
    //             .jsonPath("$.errorCode").isEqualTo("BAD_REQUEST")
    //             .jsonPath("$.trackingId").isEqualTo("myid123");

    //     Mockito.verify(dealsService, Mockito.times(0)).getActiveDeals(any(LocalTime.class));
    // }
}
