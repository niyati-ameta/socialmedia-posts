package com.intuit.socialmedia.posts.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Register the JavaTimeModule for handling Java 8 date/time types
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // Handle the exception (e.g., log it)
            e.printStackTrace();
            return null; // Or throw a custom exception
        }
    }

    public static <T> T fromJson(String jsonString, Class<T> valueType) {
        try {
            return objectMapper.readValue(jsonString, valueType);
        } catch (JsonProcessingException e) {
            // Handle the exception (e.g., log it)
            e.printStackTrace();
            return null; // Or throw a custom exception
        }
    }

}
