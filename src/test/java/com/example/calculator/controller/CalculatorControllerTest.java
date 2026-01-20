package com.example.calculator.controller;

import com.example.calculator.dto.CalculationRequest;
import com.example.calculator.dto.CalculationResponse;
import com.example.calculator.service.CalculatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CalculatorController.
 * Tests REST endpoints with mocked service layer.
 */
@WebMvcTest(CalculatorController.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CalculatorService calculatorService;

    @Test
    @DisplayName("Health endpoint returns UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Calculate endpoint returns sum and product")
    void testCalculateEndpoint() throws Exception {
        CalculationRequest request = new CalculationRequest(10.0, 5.0);
        CalculationResponse response = new CalculationResponse(10.0, 5.0, 15.0, 50.0);

        when(calculatorService.calculate(any(CalculationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operand1").value(10.0))
                .andExpect(jsonPath("$.operand2").value(5.0))
                .andExpect(jsonPath("$.sum").value(15.0))
                .andExpect(jsonPath("$.product").value(50.0));
    }

    @Test
    @DisplayName("Calculate endpoint validates missing operand1")
    void testCalculateValidationMissingOperand1() throws Exception {
        String invalidRequest = "{\"operand2\": 5.0}";

        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.operand1").exists());
    }

    @Test
    @DisplayName("Calculate endpoint validates missing operand2")
    void testCalculateValidationMissingOperand2() throws Exception {
        String invalidRequest = "{\"operand1\": 10.0}";

        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.operand2").exists());
    }

    @Test
    @DisplayName("Calculate endpoint validates empty request body")
    void testCalculateValidationEmptyBody() throws Exception {
        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Calculate endpoint handles decimal numbers")
    void testCalculateWithDecimals() throws Exception {
        CalculationRequest request = new CalculationRequest(3.14, 2.71);
        CalculationResponse response = new CalculationResponse(3.14, 2.71, 5.85, 8.5094);

        when(calculatorService.calculate(any(CalculationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(5.85))
                .andExpect(jsonPath("$.product").value(8.5094));
    }

    @Test
    @DisplayName("Calculate endpoint handles negative numbers")
    void testCalculateWithNegativeNumbers() throws Exception {
        CalculationRequest request = new CalculationRequest(-10.0, 5.0);
        CalculationResponse response = new CalculationResponse(-10.0, 5.0, -5.0, -50.0);

        when(calculatorService.calculate(any(CalculationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(-5.0))
                .andExpect(jsonPath("$.product").value(-50.0));
    }
}
