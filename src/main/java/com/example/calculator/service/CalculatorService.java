package com.example.calculator.service;

import com.example.calculator.dto.CalculationRequest;
import com.example.calculator.dto.CalculationResponse;

/**
 * Service interface for calculator operations.
 * Defines the contract for mathematical calculations.
 */
public interface CalculatorService {

    /**
     * Performs calculation on two operands.
     * Returns both sum and product of the operands.
     *
     * @param request the calculation request containing two operands
     * @return CalculationResponse with sum and product results
     */
    CalculationResponse calculate(CalculationRequest request);

    /**
     * Adds two numbers.
     *
     * @param a first operand
     * @param b second operand
     * @return sum of a and b
     */
    Double add(Double a, Double b);

    /**
     * Multiplies two numbers.
     *
     * @param a first operand
     * @param b second operand
     * @return product of a and b
     */
    Double multiply(Double a, Double b);
}
