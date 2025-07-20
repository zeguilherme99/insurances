package com.zagdev.insurances.domain.event;

import com.zagdev.insurances.domain.enums.PolicyStatus;

import java.time.Instant;
import java.util.UUID;

public class PolicyEvent {
    private UUID policyId;
    private UUID customerId;
    private PolicyStatus newStatus;
    private Instant occurredAt;

    public PolicyEvent(UUID policyId, UUID customerId, PolicyStatus newStatus) {
        this.policyId = policyId;
        this.customerId = customerId;
        this.newStatus = newStatus;
        this.occurredAt = Instant.now();
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public void setPolicyId(UUID policyId) {
        this.policyId = policyId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public PolicyStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(PolicyStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
