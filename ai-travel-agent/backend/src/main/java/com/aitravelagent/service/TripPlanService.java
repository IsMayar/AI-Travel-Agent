package com.aitravelagent.service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.aitravelagent.dto.FlightOptionResponse;
import com.aitravelagent.dto.HotelOptionResponse;
import com.aitravelagent.dto.ItineraryDayResponse;
import com.aitravelagent.dto.TripPlanRequest;
import com.aitravelagent.dto.TripPlanResponse;

@Service
public class TripPlanService {

    private static final String DEFAULT_ORIGIN = "Austin";
    private static final String DEFAULT_DESTINATION = "Dubai";
    private static final int DEFAULT_DAYS = 7;
    private static final int DEFAULT_BUDGET = 1500;

    private static final Pattern ROUTE_PATTERN = Pattern.compile(
            "\\bfrom\\s+([a-zA-Z][a-zA-Z .'-]*?)\\s+to\\s+([a-zA-Z][a-zA-Z .'-]*?)(?=\\s+(?:under|below|with|for|on|in)\\b|\\s+\\$|\\s+\\d|[.!?]*$)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DAYS_PATTERN = Pattern.compile(
            "\\b(\\d+)\\s*(?:-|\\s)?day\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern BUDGET_PATTERN = Pattern.compile(
            "(?:under|below|budget(?:\\s+of)?|less\\s+than)\\s*\\$?([\\d,]+)|\\$\\s*([\\d,]+)",
            Pattern.CASE_INSENSITIVE
    );

    public TripPlanResponse planTrip(TripPlanRequest request) {
        ParsedTripRequest parsedRequest = parseRequest(messageFrom(request));

        return new TripPlanResponse(
                parsedRequest.destination(),
                parsedRequest.origin(),
                parsedRequest.budget(),
                parsedRequest.days(),
                List.of(new FlightOptionResponse(
                        "Mock Airline",
                        850,
                        "18h 30m"
                )),
                List.of(new HotelOptionResponse(
                        "Mock Hotel " + parsedRequest.destination(),
                        90,
                        4.3
                )),
                List.of(new ItineraryDayResponse(
                        1,
                        "Arrival and hotel check-in",
                        List.of(
                                "Arrive in " + parsedRequest.destination(),
                                "Check into hotel",
                                "Evening walk"
                        )
                ))
        );
    }

    private ParsedTripRequest parseRequest(String message) {
        String origin = DEFAULT_ORIGIN;
        String destination = DEFAULT_DESTINATION;

        Matcher routeMatcher = ROUTE_PATTERN.matcher(message);
        if (routeMatcher.find()) {
            origin = cleanCity(routeMatcher.group(1), DEFAULT_ORIGIN);
            destination = cleanCity(routeMatcher.group(2), DEFAULT_DESTINATION);
        }

        int days = parseFirstInt(DAYS_PATTERN, message, DEFAULT_DAYS);
        int budget = parseBudget(message);

        return new ParsedTripRequest(origin, destination, budget, days);
    }

    private String messageFrom(TripPlanRequest request) {
        if (request == null || request.message() == null) {
            return "";
        }
        return request.message();
    }

    private int parseBudget(String message) {
        Matcher matcher = BUDGET_PATTERN.matcher(message);
        if (!matcher.find()) {
            return DEFAULT_BUDGET;
        }

        String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        return parseInt(value, DEFAULT_BUDGET);
    }

    private int parseFirstInt(Pattern pattern, String message, int defaultValue) {
        Matcher matcher = pattern.matcher(message);
        if (!matcher.find()) {
            return defaultValue;
        }
        return parseInt(matcher.group(1), defaultValue);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.replace(",", ""));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String cleanCity(String value, String defaultValue) {
        String cleaned = value
                .replaceAll("^[^a-zA-Z]+|[^a-zA-Z]+$", "")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isBlank()) {
            return defaultValue;
        }

        return titleCase(cleaned);
    }

    private String titleCase(String value) {
        String[] words = value.toLowerCase(Locale.ROOT).split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }

        return result.toString();
    }

    private record ParsedTripRequest(
            String origin,
            String destination,
            int budget,
            int days
    ) {
    }
}
