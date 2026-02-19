package com.demo.api_deals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import lombok.RequiredArgsConstructor;

@SpringBootTest(classes = ApiDealsApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
public class BaseTestClass {
    
}
