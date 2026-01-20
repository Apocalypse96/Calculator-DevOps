package com.example.calculator.service;

import com.example.calculator.dto.CalculationRequest;
import com.example.calculator.dto.CalculationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for CalculatorServiceImpl.
 * Tests mathematical operations independently.
 */
class CalculatorServiceTest {

    private CalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorServiceImpl();
    }

    @Test
    @DisplayName("Add two positive numbers")
    void testAddPositiveNumbers() {
        Double result = calculatorService.add(10.0, 5.0);
        assertEquals(15.0, result);
    }

    @Test
    @DisplayName("Add positive and negative numbers")
    void testAddMixedNumbers() {
        Double result = calculatorService.add(10.0, -5.0);
        assertEquals(5.0, result);
    }

    @Test
    @DisplayName("Add two negative numbers")
    void testAddNegativeNumbers() {
        Double result = calculatorService.add(-10.0, -5.0);
        assertEquals(-15.0, result);
    }

    @Test
    @DisplayName("Add with zero")
    void testAddWithZero() {
        Double result = calculatorService.add(10.0, 0.0);
        assertEquals(10.0, result);
    }

    @Test
    @DisplayName("Multiply two positive numbers")
    void testMultiplyPositiveNumbers() {
        Double result = calculatorService.multiply(10.0, 5.0);
        assertEquals(50.0, result);
    }

    @Test
    @DisplayName("Multiply positive and negative numbers")
    void testMultiplyMixedNumbers() {
        Double result = calculatorService.multiply(10.0, -5.0);
        assertEquals(-50.0, result);
    }

    @Test
    @DisplayName("Multiply two negative numbers")
    void testMultiplyNegativeNumbers() {
        Double result = calculatorService.multiply(-10.0, -5.0);
        assertEquals(50.0, result);
    }

    @Test
    @DisplayName("Multiply with zero")
    void testMultiplyWithZero() {
        Double result = calculatorService.multiply(10.0, 0.0);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("Calculate returns both sum and product")
    void testCalculate() {
        CalculationRequest request = new CalculationRequest(10.0, 5.0);
        CalculationResponse response = calculatorService.calculate(request);

        assertNotNull(response);
        assertEquals(10.0, response.getOperand1());
        assertEquals(5.0, response.getOperand2());
        assertEquals(15.0, response.getSum());
        assertEquals(50.0, response.getProduct());
    }

    @Test
    @DisplayName("Calculate with decimal numbers")
    void testCalculateWithDecimals() {
        CalculationRequest request = new CalculationRequest(3.5, 2.5);
        CalculationResponse response = calculatorService.calculate(request);

        assertNotNull(response);
        assertEquals(6.0, response.getSum());
        assertEquals(8.75, response.getProduct());
    }

    @Test
    @DisplayName("Add throws exception for null operand")
    void testAddNullOperand() {
        assertThrows(IllegalArgumentException.class,
                () -> calculatorService.add(null, 5.0));
        assertThrows(IllegalArgumentException.class,
                () -> calculatorService.add(5.0, null));
    }

    @Test
    @DisplayName("Multiply throws exception for null operand")
    void testMultiplyNullOperand() {
        assertThrows(IllegalArgumentException.class,
                () -> calculatorService.multiply(null, 5.0));
        assertThrows(IllegalArgumentException.class,
                () -> calculatorService.multiply(5.0, null));
    }
}
