package com.zagdev.insurances.infrastructure.dto;

import java.util.UUID;

public class MessageResult {

    private UUID policyId;
    private boolean success;

    public UUID getPolicyId() {
        return policyId;
    }

    public void setPolicyId(UUID policyId) {
        this.policyId = policyId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
