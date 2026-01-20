package com.example.calculator.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for calculation operations.
 * Contains two operands for mathematical operations.
 */
public class CalculationRequest {

    @NotNull(message = "operand1 is required")
    private Double operand1;

    @NotNull(message = "operand2 is required")
    private Double operand2;

    public CalculationRequest() {
    }

    public CalculationRequest(Double operand1, Double operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public Double getOperand1() {
        return operand1;
    }

    public void setOperand1(Double operand1) {
        this.operand1 = operand1;
    }

    public Double getOperand2() {
        return operand2;
    }

    public void setOperand2(Double operand2) {
        this.operand2 = operand2;
    }
}
