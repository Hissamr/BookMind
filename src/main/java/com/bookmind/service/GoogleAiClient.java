package com.bookmind.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Google AI (Gemini) client.
 */
@Slf4j
@Component
public class GoogleAiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String endpoint;

    public GoogleAiClient(
            ObjectMapper objectMapper,
            @Value("${google.ai.api-key}") String apiKey,
            @Value("${google.ai.endpoint}") String endpoint) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    public String generateBookSummary(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing Gemini API key: google.ai.api-key");
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            String responseBody = restClient.post()
                    .uri(endpoint)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException("Gemini API returned an empty response body");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            String text = textNode.asText("");
            if (text.isBlank()) {
                throw new IllegalStateException("Gemini response did not contain summary text");
            }

            return text;
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }
}
