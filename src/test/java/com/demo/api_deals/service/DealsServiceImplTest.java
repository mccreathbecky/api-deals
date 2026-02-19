// package com.demo.api_deals.service;

// import java.time.Duration;
// import java.time.LocalTime;

// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.beans.factory.annotation.Autowired;

// import com.demo.api_deals.BaseTestClass;
// import com.demo.api_deals.helpers.FileLoader;
// import com.demo.api_deals.model.DealResponseDto;
// import com.demo.api_deals.model.RestaurantResponseDto;
// import com.demo.api_deals.model.RestaurauntDealsResponseDto;
// import com.demo.api_deals.resource.DealsResource;

// import reactor.core.publisher.Mono;
// import reactor.test.StepVerifier;

// public class DealsServiceImplTest extends BaseTestClass {

//     @MockBean
//     private DealsResource dealsResource;

//     @Autowired
//     private DealsServiceImpl dealsService;

//     private static final FileLoader fileLoader = new FileLoader();

//     @Test
//     void testGetActiveDeals_onStandardSuccessResponse_with3pmTimeOfDay_expect5Results() {
//         // Arrange
//         // RestaurauntDealsResponseDto mockResponse = (RestaurauntDealsResponseDto)
//         // fileLoader.readFileAsObject("responses/restaurant-deals-response.json",
//         // RestaurauntDealsResponseDto.class);
//         DealResponseDto[] dealsArray = new DealResponseDto[5];
//         for (int i = 0; i < 5; i++) {
//             dealsArray[i] = DealResponseDto.builder()
//                     .discount("5")
//                     .start("2:00pm")
//                     .end("4:00pm")
//                     .build();
//         }
//         RestaurantResponseDto[] restaurantArray = new RestaurantResponseDto[1];
//         restaurantArray[0] = RestaurantResponseDto.builder()
//                 .name("My test store")
//                 .open("9:00am")
//                 .close("9:00pm")
//                 .deals(dealsArray)
//                 .build();

//         RestaurauntDealsResponseDto mockResponse = RestaurauntDealsResponseDto.builder()
//                 .restaurants(restaurantArray)
//                 .build();

//         LocalTime timeOfDay = LocalTime.of(15, 0); // 3:00 PM

//         Mockito.when(dealsResource.getAllDeals())
//                 .thenReturn(Mono.just(mockResponse)
//                         .delayElement(Duration.ofMillis(500))); // Simulate some delay in response

//         // Act
//         StepVerifier.create(dealsService.getActiveDeals(timeOfDay))

//                 // Assert
//                 .expectNextMatches(activeDealsResponse -> activeDealsResponse.getDeals() != null
//                         && activeDealsResponse.getDeals().size() == 5)
//                 .expectComplete()
//                 .verify();

//         Mockito.verify(dealsResource, Mockito.times(1)).getAllDeals();
//     }

//     @Test
//     void testGetPeakDeals() {

//     }
// }
