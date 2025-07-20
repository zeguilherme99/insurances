package com.zagdev.insurances.infrastructure.dto;

import com.zagdev.insurances.domain.enums.RiskClassification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class FraudAnalysisResponse {
    private UUID orderId;
    private UUID customerId;
    private OffsetDateTime analyzedAt;
    private RiskClassification classification;
    private List<Occurrence> occurrences;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public OffsetDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(OffsetDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public RiskClassification getClassification() {
        return classification;
    }

    public void setClassification(RiskClassification classification) {
        this.classification = classification;
    }

    public List<Occurrence> getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(List<Occurrence> occurrences) {
        this.occurrences = occurrences;
    }

    public static class Occurrence {
        private UUID id;
        private long productId;
        private String type;
        private String description;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public long getProductId() {
            return productId;
        }

        public void setProductId(long productId) {
            this.productId = productId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public OffsetDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public OffsetDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
