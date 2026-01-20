package com.example.calculator.dto;

/**
 * Response DTO containing the results of calculation operations.
 * Includes both sum and product of the input operands.
 */
public class CalculationResponse {

    private Double operand1;
    private Double operand2;
    private Double sum;
    private Double product;

    public CalculationResponse() {
    }

    public CalculationResponse(Double operand1, Double operand2, Double sum, Double product) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.sum = sum;
        this.product = product;
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

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Double getProduct() {
        return product;
    }

    public void setProduct(Double product) {
        this.product = product;
    }
}
