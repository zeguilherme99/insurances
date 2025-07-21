package com.zagdev.insurances.infrastructure.implementation;

import com.zagdev.insurances.domain.enums.RiskClassification;
import com.zagdev.insurances.domain.exceptions.ErrorCode;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.infrastructure.FraudApiClient;
import com.zagdev.insurances.infrastructure.dto.FraudAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class FraudApiClientImpl implements FraudApiClient {

    private static final Logger logger = LoggerFactory.getLogger(FraudApiClientImpl.class);


    private final RestTemplate restTemplate;

    public FraudApiClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public RiskClassification getRiskClassification(UUID orderId, UUID customerId) throws InvalidDataException {
        String url = String.format("http://localhost:8081/frauds/%s/customers/%s", orderId, customerId);
        logger.info("Calling Fraud API for orderId [{}] and customerId [{}] at URL [{}]", orderId, customerId, url);

        FraudAnalysisResponse response = restTemplate.getForObject(url, FraudAnalysisResponse.class);

        if (response == null || response.getClassification() == null) {
            logger.error("Fraud API returned no classification for orderId [{}], customerId [{}]", orderId, customerId);
            throw new InvalidDataException(ErrorCode.INVALID_DATA);
        }

        logger.info("Fraud API returned classification [{}] for orderId [{}], customerId [{}]",
                response.getClassification(), orderId, customerId);

        return response.getClassification();
    }
}
