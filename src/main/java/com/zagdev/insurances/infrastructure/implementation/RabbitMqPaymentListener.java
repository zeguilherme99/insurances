package com.zagdev.insurances.infrastructure.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import com.zagdev.insurances.infrastructure.dto.PaymentResult;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqPaymentListener {

    private final PolicyUseCase policyUseCase;
    private final ObjectMapper objectMapper;

    public RabbitMqPaymentListener(PolicyUseCase policyUseCase, ObjectMapper objectMapper) {
        this.policyUseCase = policyUseCase;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "payment-result-queue")
    public void handlePaymentResult(String payload) throws JsonProcessingException {
        System.out.println("Recebido do RabbitMQ: " + payload);

        PaymentResult result = objectMapper.readValue(payload, PaymentResult.class);
        if (result.isSuccess()) {
            policyUseCase.approve(result.getPolicyId());
        } else {
            policyUseCase.reject(result.getPolicyId());
        }
    }
}
