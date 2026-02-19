package com.demo.api_deals.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.demo.api_deals.BaseTestClass;
import com.demo.api_deals.helpers.FileLoader;
import com.demo.api_deals.model.DealsError;
import com.demo.api_deals.model.RestaurauntDealsResponseDto;
import com.demo.api_deals.resource.DealsResource;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class DealsServiceImplTest extends BaseTestClass {

    @MockitoBean
    private DealsResource dealsResource;

    @Autowired
    private DealsServiceImpl dealsService;

    private final FileLoader fileLoader = new FileLoader();
    private final RestaurauntDealsResponseDto mockResponse = (RestaurauntDealsResponseDto) fileLoader.readFileAsObject("responses/restaurant-deals-response.json", RestaurauntDealsResponseDto.class);

    @Test
    void testGetActiveDeals_onStandardSuccessResponse_with3pmTimeOfDay_expect5Results() {
        // Arrange
        // Use shared mock response already initiated
        LocalTime timeOfDay = LocalTime.of(15, 0); // 3:00 PM

        Mockito.when(dealsResource.getAllDeals())
                .thenReturn(Mono.just(mockResponse)
                        .delayElement(Duration.ofMillis(500))); // Simulate some delay in response

        // Act
        StepVerifier.create(dealsService.getActiveDeals(timeOfDay))
                // Assert
                .thenConsumeWhile( activeDealsResponse -> 
                        {
                        assertNotNull(activeDealsResponse.getDeals());
                        assertEquals(5, activeDealsResponse.getDeals().size(), "Expected 5 active deals");
                        assertEquals("Masala Kitchen", activeDealsResponse.getDeals().get(0).getRestaurantName(), "Expected restaurant name to match");
                        assertEquals("DEA567C5-0000-3C03-FF00-E3B24909BE00", activeDealsResponse.getDeals().get(0).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("DEA567C5-1111-3C03-FF00-E3B24909BE01", activeDealsResponse.getDeals().get(1).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("D80263E8-0000-2C70-FF6B-D854ADB8DB02", activeDealsResponse.getDeals().get(2).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("D80263E8-1111-2C70-FF6B-D854ADB8DB03", activeDealsResponse.getDeals().get(3).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("B5713CD0-0000-40C7-AFC3-7D46D26B00BF", activeDealsResponse.getDeals().get(4).getDealObjectId(), "Expected deal object ID to match");
                        return true; // Continue consuming if all assertions pass
                })
                .verifyComplete();

        Mockito.verify(dealsResource, Mockito.times(1)).getAllDeals();
    }


    @Test
    void testGetActiveDeals_onStandardSuccessResponse_with6pmTimeOfDay_expect9Results() {
        // Arrange
        // Use shared mock response already initiated
        LocalTime timeOfDay = LocalTime.of(18, 0); // 6:00 PM

        Mockito.when(dealsResource.getAllDeals())
                .thenReturn(Mono.just(mockResponse)
                        .delayElement(Duration.ofMillis(500))); // Simulate some delay in response

        // Act
        StepVerifier.create(dealsService.getActiveDeals(timeOfDay))
                // Assert
                .thenConsumeWhile(activeDealsResponse -> 
                        {
                        assertNotNull(activeDealsResponse.getDeals());
                        assertEquals(9, activeDealsResponse.getDeals().size(), "Expected 9 active deals");
                        assertEquals("Masala Kitchen", activeDealsResponse.getDeals().get(0).getRestaurantName(), "Expected restaurant name to match");
                        assertEquals("DEA567C5-0000-3C03-FF00-E3B24909BE00", activeDealsResponse.getDeals().get(0).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("DEA567C5-1111-3C03-FF00-E3B24909BE01", activeDealsResponse.getDeals().get(1).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("D80263E8-0000-2C70-FF6B-D854ADB8DB02", activeDealsResponse.getDeals().get(2).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("D80263E8-1111-2C70-FF6B-D854ADB8DB03", activeDealsResponse.getDeals().get(3).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("CDB2B42A-0000-EE20-FF45-8D0A8057E204", activeDealsResponse.getDeals().get(4).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("B5713CD0-0000-40C7-AFC3-7D46D26B00BF", activeDealsResponse.getDeals().get(5).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("B5713CD0-1111-40C7-AFC3-7D46D26B00BF", activeDealsResponse.getDeals().get(6).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("B5913CD0-0000-40C7-AFC3-7D46D26B01BF", activeDealsResponse.getDeals().get(7).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("B5713CD0-1111-40C7-AFC3-7D46D26B00BF", activeDealsResponse.getDeals().get(8).getDealObjectId(), "Expected deal object ID to match");
                        return true; // Continue consuming if all assertions pass
                        })
                .verifyComplete();

        Mockito.verify(dealsResource, Mockito.times(1)).getAllDeals();
    }


    @Test
    void testGetActiveDeals_onStandardSuccessResponse_with9pmTimeOfDay_expect4Results() {
        // Arrange
        // Use shared mock response already initiated
        LocalTime timeOfDay = LocalTime.of(21, 0); // 9:00 PM

        Mockito.when(dealsResource.getAllDeals())
                .thenReturn(Mono.just(mockResponse)
                        .delayElement(Duration.ofMillis(500))); // Simulate some delay in response

        // Act
        StepVerifier.create(dealsService.getActiveDeals(timeOfDay))
                // Assert
                .thenConsumeWhile(activeDealsResponse -> 
                        {
                        assertNotNull(activeDealsResponse.getDeals());
                        assertEquals(4, activeDealsResponse.getDeals().size(), "Expected 4 active deals");
                        assertEquals("ABC Chicken", activeDealsResponse.getDeals().get(0).getRestaurantName(), "Expected restaurant name to match");
                        assertEquals("D80263E8-0000-2C70-FF6B-D854ADB8DB02", activeDealsResponse.getDeals().get(0).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("D80263E8-1111-2C70-FF6B-D854ADB8DB03", activeDealsResponse.getDeals().get(1).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("B5913CD0-0000-40C7-AFC3-7D46D26B01BF", activeDealsResponse.getDeals().get(2).getDealObjectId(), "Expected deal object ID to match");
                        assertEquals("B5713CD0-1111-40C7-AFC3-7D46D26B00BF", activeDealsResponse.getDeals().get(3).getDealObjectId(), "Expected deal object ID to match");
                        return true; // Continue consuming if all assertions pass
                        })
                .verifyComplete();

        Mockito.verify(dealsResource, Mockito.times(1)).getAllDeals();
    }


    @Test
    void testGetActiveDeals_onNullPointerException_expectDealsErrorThrown() {
        // Arrange
        LocalTime timeOfDay = LocalTime.of(15, 0); // 3:00 PM

        Mockito.when(dealsResource.getAllDeals())
            .thenReturn(Mono.error(new NullPointerException("Simulated null pointer exception")));

        // Act
        StepVerifier.create(dealsService.getActiveDeals(timeOfDay))
            // Assert
            .consumeErrorWith(throwable -> 
                {
                    assertTrue(throwable instanceof DealsError);
                    DealsError dealsError = (DealsError) throwable;
                    assertEquals("Failed to retrieve deals data: Simulated null pointer exception", dealsError.getMessage());
                    assertEquals("Internal Server Error", dealsError.getErrorCode());
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, dealsError.getHttpStatus());
                }
            );

        Mockito.verify(dealsResource, Mockito.times(1)).getAllDeals();
    }

    @Test
    void testGetActiveDeals_onRuntimeException_expectDealsErrorThrown() {
        // Arrange
        LocalTime timeOfDay = LocalTime.of(15, 0); // 3:00 PM

        Mockito.when(dealsResource.getAllDeals())
            .thenReturn(Mono.error(new RuntimeException("Simulated runtime exception")));

        // Act
        StepVerifier.create(dealsService.getActiveDeals(timeOfDay))
            // Assert
            .consumeErrorWith(throwable -> 
                {
                    assertTrue(throwable instanceof DealsError);
                    DealsError dealsError = (DealsError) throwable;
                    assertEquals("Failed to retrieve deals data: Simulated runtime exception", dealsError.getMessage());
                    assertEquals("Internal Server Error", dealsError.getErrorCode());
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, dealsError.getHttpStatus());
                }
            );

        Mockito.verify(dealsResource, Mockito.times(1)).getAllDeals();
    }

    @Test
    void testGetPeakDeals() {

    }
}
