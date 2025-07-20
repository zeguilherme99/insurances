package com.zagdev.insurances.domain.entity;

import com.zagdev.insurances.domain.enums.InsuranceCategory;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Document(collection = "policies")
public class Policy {
    private UUID id;
    private UUID customerId;
    private UUID productId;
    private InsuranceCategory category;
    private String salesChannel;
    private String paymentMethod;
    private BigDecimal totalMonthlyPremium;
    private BigDecimal insuredAmount;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    private PolicyStatus status;
    private Instant createdAt;
    private Instant finishedAt;
    private List<StatusHistory> history;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public InsuranceCategory getCategory() {
        return category;
    }

    public void setCategory(InsuranceCategory category) {
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

    public BigDecimal getTotalMonthlyPremium() {
        return totalMonthlyPremium;
    }

    public void setTotalMonthlyPremium(BigDecimal totalMonthlyPremium) {
        this.totalMonthlyPremium = totalMonthlyPremium;
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

    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public List<StatusHistory> getHistory() {
        return history;
    }

    public void setHistory(List<StatusHistory> history) {
        this.history = history;
    }

    public static class StatusHistory {
        private PolicyStatus status;
        private Instant timestamp;

        public PolicyStatus getStatus() {
            return status;
        }

        public void setStatus(PolicyStatus status) {
            this.status = status;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }
    }
}
