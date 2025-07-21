package com.zagdev.insurances.infrastructure;

import com.zagdev.insurances.domain.enums.RiskClassification;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;

import java.util.UUID;

public interface FraudApiClient {

    RiskClassification getRiskClassification(UUID orderId, UUID customerId) throws InvalidDataException;
}
