package com.aitravelagent;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TripPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void planTripReturnsMockTripPlan() throws Exception {
        mockMvc.perform(post("/api/trips/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Plan a 7-day trip from Austin to Dubai under $1500"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.destination").value("Dubai"))
                .andExpect(jsonPath("$.origin").value("Austin"))
                .andExpect(jsonPath("$.budget").value(1500))
                .andExpect(jsonPath("$.days").value(7))
                .andExpect(jsonPath("$.flightOptions[0].airline").value("Mock Airline"))
                .andExpect(jsonPath("$.flightOptions[0].price").value(850))
                .andExpect(jsonPath("$.flightOptions[0].duration").value("18h 30m"))
                .andExpect(jsonPath("$.hotelOptions[0].name").value("Mock Hotel Dubai"))
                .andExpect(jsonPath("$.hotelOptions[0].pricePerNight").value(90))
                .andExpect(jsonPath("$.hotelOptions[0].rating").value(4.3))
                .andExpect(jsonPath("$.itinerary[0].day").value(1))
                .andExpect(jsonPath("$.itinerary[0].title").value("Arrival and hotel check-in"))
                .andExpect(jsonPath("$.itinerary[0].activities[0]").value("Arrive in Dubai"))
                .andExpect(jsonPath("$.itinerary[0].activities[1]").value("Check into hotel"))
                .andExpect(jsonPath("$.itinerary[0].activities[2]").value("Evening walk"));
    }

    @Test
    void planTripParsesTripDetailsFromMessage() throws Exception {
        mockMvc.perform(post("/api/trips/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Plan a 3-day trip from Kabul to Tokyo under $2500"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("Tokyo"))
                .andExpect(jsonPath("$.origin").value("Kabul"))
                .andExpect(jsonPath("$.budget").value(2500))
                .andExpect(jsonPath("$.days").value(3))
                .andExpect(jsonPath("$.hotelOptions[0].name").value("Mock Hotel Tokyo"))
                .andExpect(jsonPath("$.itinerary[0].activities[0]").value("Arrive in Tokyo"));
    }

    @Test
    void planTripUsesDefaultsWhenMessageIsMissingDetails() throws Exception {
        mockMvc.perform(post("/api/trips/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Please plan something fun"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("Dubai"))
                .andExpect(jsonPath("$.origin").value("Austin"))
                .andExpect(jsonPath("$.budget").value(1500))
                .andExpect(jsonPath("$.days").value(7));
    }

    @Test
    void planTripAllowsLocalhostFrontendCors() throws Exception {
        mockMvc.perform(post("/api/trips/plan")
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Plan a 7-day trip from Austin to Dubai under $1500"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
