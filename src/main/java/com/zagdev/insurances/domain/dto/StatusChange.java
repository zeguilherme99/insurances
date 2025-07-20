package com.zagdev.insurances.domain.dto;

import com.zagdev.insurances.domain.enums.PolicyStatus;

import java.time.Instant;

public class StatusChange {
    private PolicyStatus status;
    private Instant timestamp;

    public StatusChange(PolicyStatus status, Instant timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }

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
