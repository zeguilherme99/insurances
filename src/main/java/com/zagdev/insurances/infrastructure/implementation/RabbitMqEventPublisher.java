package com.zagdev.insurances.infrastructure.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zagdev.insurances.domain.event.PolicyEvent;
import com.zagdev.insurances.domain.exceptions.ErrorCode;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;
import com.zagdev.insurances.infrastructure.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqEventPublisher implements EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitMqEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(PolicyEvent event) throws InvalidDataException, UnexpectedErrorException {
        try {
            String payload = objectMapper.writeValueAsString(event);
            logger.info("Publisher: Publishing event for policy [{}], customer [{}], status [{}] to exchange [{}] with routingKey [{}]",
                    event.getPolicyId(), event.getCustomerId(), event.getNewStatus(),
                    "policy-exchange", "policy.status.changed");
            rabbitTemplate.convertAndSend("policy-exchange", "policy.status.changed", payload);
            logger.info("Publisher: Event published successfully for policy [{}]", event.getPolicyId());
        } catch (JsonProcessingException e) {
            logger.error("Publisher: Error serializing event for policy [{}]: {}", event.getPolicyId(), e.getMessage(), e);
            throw new InvalidDataException(ErrorCode.INVALID_DATA, e);
        } catch (Exception e) {
            logger.error("Publisher: Error publishing event for policy [{}]: {}", event.getPolicyId(), e.getMessage(), e);
            throw new UnexpectedErrorException(ErrorCode.UNEXPECTED_ERROR, e);
        }
    }
}
