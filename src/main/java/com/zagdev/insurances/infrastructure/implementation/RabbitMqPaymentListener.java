package com.zagdev.insurances.infrastructure.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import com.zagdev.insurances.infrastructure.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqPaymentListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqPaymentListener.class);

    private final PolicyUseCase policyUseCase;
    private final ObjectMapper objectMapper;

    public RabbitMqPaymentListener(PolicyUseCase policyUseCase, ObjectMapper objectMapper) {
        this.policyUseCase = policyUseCase;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "payment-result-queue")
    public void handlePaymentResult(String payload) throws JsonProcessingException, DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("RabbitMQ Listener: Received message from 'payment-result-queue': [{}]", payload);

        PaymentResult result = objectMapper.readValue(payload, PaymentResult.class);

        if (result.isSuccess()) {
            logger.info("RabbitMQ Listener: Payment success for policy [{}]. Approving policy.", result.getPolicyId());
            policyUseCase.approve(result.getPolicyId());
        } else {
            logger.info("RabbitMQ Listener: Payment failed for policy [{}]. Rejecting policy.", result.getPolicyId());
            policyUseCase.reject(result.getPolicyId());
        }

        logger.info("RabbitMQ Listener: Policy [{}] processed successfully.", result.getPolicyId());
    }
}
