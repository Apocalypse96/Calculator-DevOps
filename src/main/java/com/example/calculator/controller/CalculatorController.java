package com.example.calculator.controller;

import com.example.calculator.dto.CalculationRequest;
import com.example.calculator.dto.CalculationResponse;
import com.example.calculator.service.CalculatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for calculator operations.
 * Provides health check and calculation endpoints.
 */
@RestController
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    /**
     * Health check endpoint.
     * Returns OK status when the application is running.
     *
     * @return health status response
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    /**
     * Calculation endpoint.
     * Accepts two operands and returns their sum and product.
     *
     * @param request the calculation request with two operands
     * @return calculation response with sum and product
     */
    @PostMapping("/calculate")
    public ResponseEntity<CalculationResponse> calculate(
            @Valid @RequestBody CalculationRequest request) {
        CalculationResponse response = calculatorService.calculate(request);
        return ResponseEntity.ok(response);
    }
}
