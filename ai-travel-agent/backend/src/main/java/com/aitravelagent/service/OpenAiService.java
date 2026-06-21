package com.aitravelagent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenAiService {

    private final String apiKey;

    public OpenAiService(@Value("${openai.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String generateTripPlan(String userMessage) {
        String keyStatus = apiKey == null || apiKey.isBlank()
                ? "OPENAI_API_KEY is not configured"
                : "OPENAI_API_KEY is configured";

        return "Mocked AI trip plan response. " + keyStatus + ". Request: " + userMessage;
    }
}
