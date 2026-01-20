package com.example.calculator.service;

import com.example.calculator.dto.CalculationRequest;
import com.example.calculator.dto.CalculationResponse;
import org.springframework.stereotype.Service;

/**
 * Implementation of CalculatorService.
 * Provides mathematical operations for the calculator API.
 */
@Service
public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public CalculationResponse calculate(CalculationRequest request) {
        Double operand1 = request.getOperand1();
        Double operand2 = request.getOperand2();

        Double sum = add(operand1, operand2);
        Double product = multiply(operand1, operand2);

        return new CalculationResponse(operand1, operand2, sum, product);
    }

    @Override
    public Double add(Double a, Double b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Operands cannot be null");
        }
        return a + b;
    }

    @Override
    public Double multiply(Double a, Double b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Operands cannot be null");
        }
        return a * b;
    }
}
