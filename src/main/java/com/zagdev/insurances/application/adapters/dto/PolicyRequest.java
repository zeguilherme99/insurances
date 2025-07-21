package com.zagdev.insurances.application.adapters.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PolicyRequest {

    @JsonProperty("customer_id")
    private UUID customerId;
    @JsonProperty("product_id")
    private UUID productId;
    private String category;
    private String salesChannel;
    private String paymentMethod;
    @JsonProperty("total_monthly_premium_amount")
    private BigDecimal totalMonthlyPremiumAmount;
    @JsonProperty("insured_amount")
    private BigDecimal insuredAmount;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getTotalMonthlyPremiumAmount() {
        return totalMonthlyPremiumAmount;
    }

    public void setTotalMonthlyPremiumAmount(BigDecimal totalMonthlyPremiumAmount) {
        this.totalMonthlyPremiumAmount = totalMonthlyPremiumAmount;
    }

    public BigDecimal getInsuredAmount() {
        return insuredAmount;
    }

    public void setInsuredAmount(BigDecimal insuredAmount) {
        this.insuredAmount = insuredAmount;
    }

    public Map<String, BigDecimal> getCoverages() {
        return coverages;
    }

    public void setCoverages(Map<String, BigDecimal> coverages) {
        this.coverages = coverages;
    }

    public List<String> getAssistances() {
        return assistances;
    }

    public void setAssistances(List<String> assistances) {
        this.assistances = assistances;
    }
}
