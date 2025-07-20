package com.zagdev.insurances.application.adapters.dto;

import com.zagdev.insurances.domain.enums.PolicyStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PolicyResponse {

    public UUID id;
    public UUID customerId;
    public UUID productId;
    public String category;
    public String salesChannel;
    public String paymentMethod;
    public PolicyStatus status;
    public Instant createdAt;
    public Instant finishedAt;
    public BigDecimal totalMonthlyPremiumAmount;
    public BigDecimal insuredAmount;
    public Map<String, BigDecimal> coverages;
    public List<String> assistances;
    public List<StatusHistoryDTO> history;

    public static class StatusHistoryDTO {
        public PolicyStatus status;
        public Instant timestamp;
    }
}
