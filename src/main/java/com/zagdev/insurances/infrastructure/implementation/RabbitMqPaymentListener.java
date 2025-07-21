package com.zagdev.insurances.infrastructure.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import com.zagdev.insurances.infrastructure.dto.MessageResult;
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

        MessageResult result = objectMapper.readValue(payload, MessageResult.class);

        if (result.isSuccess()) {
            logger.info("RabbitMQ Listener: Payment success for policy [{}]. Approving policy.", result.getPolicyId());
            PolicyDTO policy = policyUseCase.setPaymentConfirmed(result.getPolicyId());

            if (policy.getSubscriptionAuthorized()) {
                policyUseCase.approve(result.getPolicyId());
                logger.info("RabbitMQ Listener: Policy [{}] approved (payment + subscription).", result.getPolicyId());
            }
        } else {
            logger.info("RabbitMQ Listener: Payment failed for policy [{}]. Rejecting policy.", result.getPolicyId());
            policyUseCase.reject(result.getPolicyId());
        }

        logger.info("RabbitMQ Listener: Policy [{}] processed successfully.", result.getPolicyId());
    }

    @RabbitListener(queues = "subscription-result-queue")
    public void handleSubscriptionResult(String payload) throws JsonProcessingException, DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("RabbitMQ Listener: Received message from 'subscription-result-queue': [{}]", payload);

        MessageResult result = objectMapper.readValue(payload, MessageResult.class);

        if (result.isSuccess()) {
            logger.info("RabbitMQ Listener: Subscription approved for policy [{}].", result.getPolicyId());
            PolicyDTO policy = policyUseCase.setSubscriptionAuthorized(result.getPolicyId());

            if (policy.getPaymentConfirmed()) {
                policyUseCase.approve(result.getPolicyId());
                logger.info("RabbitMQ Listener: Policy [{}] approved (payment + subscription).", result.getPolicyId());
            }
        } else {
            logger.info("RabbitMQ Listener: Subscription failed for policy [{}]. Rejecting policy.", result.getPolicyId());
            policyUseCase.reject(result.getPolicyId());
        }

        logger.info("RabbitMQ Listener: Policy [{}] processed successfully.", result.getPolicyId());
    }
}
