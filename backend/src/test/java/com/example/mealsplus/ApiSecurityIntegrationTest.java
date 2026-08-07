package com.example.mealsplus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void demoSeniorCanLoginAndCreateMealRequest() throws Exception {
        String token = login("senior@mealsplus.local", "Senior123!");
        String body = "{\"requestedDeliveryDate\":\"" + LocalDate.now().plusDays(1) + "\",\"mealType\":\"Soup\",\"quantity\":1,\"deliveryAddress\":\"123 Main\"}";
        mockMvc.perform(post("/api/meal-requests").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void seniorCannotListAdminUsers() throws Exception {
        String token = login("senior@mealsplus.local", "Senior123!");
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void volunteerCannotReadAllMealRequests() throws Exception {
        String token = login("volunteer@mealsplus.local", "Volunteer123!");
        mockMvc.perform(get("/api/meal-requests").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test void seniorCannotAccessRoboCompanionInventory() throws Exception {
        String token = login("senior@mealsplus.local", "Senior123!");
        mockMvc.perform(get("/api/robocompanions").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test void volunteerCannotAccessRoboCompanionRequests() throws Exception {
        String token = login("volunteer@mealsplus.local", "Volunteer123!");
        mockMvc.perform(get("/api/robocompanion-requests").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void invalidTokenReturnsUnauthorizedJson() throws Exception {
        mockMvc.perform(get("/api/profile").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").exists());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }
}
