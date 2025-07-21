package com.zagdev.insurances.infrastructure.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import com.zagdev.insurances.domain.event.PolicyEvent;
import com.zagdev.insurances.domain.exceptions.ErrorCode;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitMqEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private ObjectMapper objectMapper;

    private RabbitMqEventPublisher publisher;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        publisher = new RabbitMqEventPublisher(rabbitTemplate, objectMapper);
    }

    @Test
    void shouldPublishEventSuccessfully() throws Exception {
        PolicyEvent event = new PolicyEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                PolicyStatus.APPROVED
        );
        String payload = "{\"id\": \"1\", \"status\": \"APPROVED\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(payload);

        publisher.publish(event);

        verify(objectMapper).writeValueAsString(event);
        verify(rabbitTemplate).convertAndSend("policy-exchange", "policy.status.changed", payload);
    }

    @Test
    void shouldThrowWhenSerializationFails() throws Exception {
        PolicyEvent event = new PolicyEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                PolicyStatus.APPROVED
        );

        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("erro!") {});

        Exception ex = assertThrows(InvalidDataException.class, () -> publisher.publish(event));
        assertEquals(ex.getMessage(), ErrorCode.INVALID_DATA.getMessage());
        verify(objectMapper).writeValueAsString(event);
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
