package com.zagdev.insurances.domain.dto;

import com.zagdev.insurances.domain.enums.InsuranceCategory;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import com.zagdev.insurances.domain.enums.RiskClassification;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.rules.RiskRules;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.zagdev.insurances.domain.exceptions.ErrorCode.INVALID_STATUS;

public class PolicyDTO {

    private UUID id;
    private UUID customerId;
    private UUID productId;
    private InsuranceCategory category;
    private String salesChannel;
    private String paymentMethod;
    private Boolean paymentConfirmed;
    private Boolean subscriptionAuthorized;
    private BigDecimal totalMonthlyPremium;
    private BigDecimal insuredAmount;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    private PolicyStatus status;
    private Instant createdAt;
    private Instant finishedAt;
    private List<StatusChange> history = new ArrayList<>();

    public PolicyDTO(UUID customerId, UUID productId, InsuranceCategory category, String salesChannel,
                  String paymentMethod, BigDecimal totalMonthlyPremium, BigDecimal insuredAmount,
                  Map<String, BigDecimal> coverages, List<String> assistances) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.productId = productId;
        this.category = category;
        this.salesChannel = salesChannel;
        this.paymentMethod = paymentMethod;
        this.paymentConfirmed = false;
        this.subscriptionAuthorized = false;
        this.totalMonthlyPremium = totalMonthlyPremium;
        this.insuredAmount = insuredAmount;
        this.coverages = coverages;
        this.assistances = assistances;
        this.status = PolicyStatus.RECEIVED;
        this.createdAt = Instant.now();
        addHistory(this.status);
    }

    public PolicyDTO() {
    }

    public void validate(RiskClassification classification) throws InvalidDataException {
        if (status == PolicyStatus.CANCELLED) {
            throw new InvalidDataException(INVALID_STATUS);
        }

        boolean approved = RiskRules.isApproved(classification, category, insuredAmount);

        if (approved) {
            transitionTo(PolicyStatus.VALIDATED);
        } else {
            transitionTo(PolicyStatus.REJECTED);
        }
    }

    public void markAsPending() throws InvalidDataException {
        if (status != PolicyStatus.VALIDATED) {
            throw new InvalidDataException(INVALID_STATUS);
        }
        transitionTo(PolicyStatus.PENDING);
    }

    public void approve() throws InvalidDataException {
        if (status != PolicyStatus.PENDING) {
            throw new InvalidDataException(INVALID_STATUS);
        }

        transitionTo(PolicyStatus.APPROVED);
        this.finishedAt = Instant.now();
    }

    public void reject() throws InvalidDataException {
        if (status != PolicyStatus.PENDING && status != PolicyStatus.VALIDATED) {
            throw new InvalidDataException(INVALID_STATUS);
        }
        transitionTo(PolicyStatus.REJECTED);
        this.finishedAt = Instant.now();
    }

    public void cancel() throws InvalidDataException {
        if (status == PolicyStatus.APPROVED || status == PolicyStatus.REJECTED || status == PolicyStatus.CANCELLED) {
            throw new InvalidDataException(INVALID_STATUS);
        }
        transitionTo(PolicyStatus.CANCELLED);
        this.finishedAt = Instant.now();
    }

    private void transitionTo(PolicyStatus newStatus) {
        this.status = newStatus;
        addHistory(newStatus);
    }

    private void addHistory(PolicyStatus status) {
        this.history.add(new StatusChange(status, Instant.now()));
    }

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

    public Boolean getPaymentConfirmed() {
        return paymentConfirmed;
    }

    public void setPaymentConfirmed(Boolean paymentConfirmed) {
        this.paymentConfirmed = paymentConfirmed;
    }

    public Boolean getSubscriptionAuthorized() {
        return subscriptionAuthorized;
    }

    public void setSubscriptionAuthorized(Boolean subscriptionAuthorized) {
        this.subscriptionAuthorized = subscriptionAuthorized;
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

    public List<StatusChange> getHistory() {
        return history;
    }

    public void setHistory(List<StatusChange> history) {
        this.history = history;
    }
}
