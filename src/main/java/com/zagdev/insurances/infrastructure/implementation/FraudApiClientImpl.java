package com.zagdev.insurances.infrastructure.implementation;

import com.zagdev.insurances.domain.enums.RiskClassification;
import com.zagdev.insurances.infrastructure.FraudApiClient;
import com.zagdev.insurances.infrastructure.dto.FraudAnalysisResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class FraudApiClientImpl implements FraudApiClient {

    private final RestTemplate restTemplate;

    public FraudApiClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public RiskClassification getRiskClassification(UUID orderId, UUID customerId) {
        String url = String.format("http://localhost:8081/frauds/%s/customers/%s", orderId, customerId);

        FraudAnalysisResponse response = restTemplate.getForObject(url, FraudAnalysisResponse.class);

        if (response == null || response.getClassification() == null) {
            throw new IllegalStateException("Classificação de risco não encontrada");
        }

        return response.getClassification();
    }
}
