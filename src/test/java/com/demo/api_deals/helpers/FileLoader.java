package com.demo.api_deals.helpers;

import java.io.InputStream;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class FileLoader<T>{

    private final ObjectMapper objectMapper = new ObjectMapper();

    public T readFileAsObject(String filePath, Class<T> clazz) {
        try {
            ClassLoader classLoader = clazz.getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(filePath);
            return (T) objectMapper.readValue(inputStream, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    public String readFileAsString(String filePath, Class<T> clazz) {
        try {
            ClassLoader classLoader = clazz.getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(filePath);
            return new String(inputStream.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
}
