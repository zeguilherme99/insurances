package com.zagdev.insurances.infrastructure.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import com.zagdev.insurances.infrastructure.dto.MessageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RabbitMqPaymentListenerTest {

    @Mock
    private PolicyUseCase policyUseCase;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RabbitMqPaymentListener listener;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldApproveWhenPaymentAndSubscriptionIsSuccess() throws Exception {
        UUID policyId = UUID.randomUUID();
        String payload = "{\"policyId\": \"" + policyId + "\", \"success\": true}";

        MessageResult messageResult = new MessageResult();
        messageResult.setPolicyId(policyId);
        messageResult.setSuccess(true);

        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setSubscriptionAuthorized(true);

        when(objectMapper.readValue(payload, MessageResult.class)).thenReturn(messageResult);
        when(policyUseCase.setPaymentConfirmed(eq(policyId))).thenReturn(policyDTO);
        listener.handlePaymentResult(payload);

        verify(policyUseCase).approve(policyId);
        verify(policyUseCase, never()).reject(any());
    }

    @Test
    void shouldRejectWhenPaymentIsNotSuccess() throws Exception {
        UUID policyId = UUID.randomUUID();
        String payload = "{\"policyId\": \"" + policyId + "\", \"success\": false}";

        MessageResult messageResult = new MessageResult();
        messageResult.setPolicyId(policyId);
        messageResult.setSuccess(false);

        when(objectMapper.readValue(payload, MessageResult.class)).thenReturn(messageResult);

        listener.handlePaymentResult(payload);

        verify(policyUseCase).reject(policyId);
        verify(policyUseCase, never()).approve(any());
    }

    @Test
    void shouldThrowWhenDeserializationFails() throws Exception {
        String payload = "INVALID_JSON";

        when(objectMapper.readValue(payload, MessageResult.class))
                .thenThrow(new JsonProcessingException("Erro de parsing!") {});

        assertThrows(JsonProcessingException.class,
                () -> listener.handlePaymentResult(payload));

        verifyNoInteractions(policyUseCase);
    }

    @Test
    void shouldApproveWhenSubscriptionAndPaymentIsSuccess() throws Exception {
        UUID policyId = UUID.randomUUID();
        String payload = "{\"policyId\": \"" + policyId + "\", \"success\": true}";

        MessageResult messageResult = new MessageResult();
        messageResult.setPolicyId(policyId);
        messageResult.setSuccess(true);

        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setPaymentConfirmed(true);

        when(objectMapper.readValue(payload, MessageResult.class)).thenReturn(messageResult);
        when(policyUseCase.setSubscriptionAuthorized(eq(policyId))).thenReturn(policyDTO);
        listener.handleSubscriptionResult(payload);

        verify(policyUseCase).approve(policyId);
        verify(policyUseCase, never()).reject(any());
    }

    @Test
    void shouldRejectWhenSubscriptionIsNotSuccess() throws Exception {
        UUID policyId = UUID.randomUUID();
        String payload = "{\"policyId\": \"" + policyId + "\", \"success\": false}";

        MessageResult messageResult = new MessageResult();
        messageResult.setPolicyId(policyId);
        messageResult.setSuccess(false);

        when(objectMapper.readValue(payload, MessageResult.class)).thenReturn(messageResult);

        listener.handleSubscriptionResult(payload);

        verify(policyUseCase).reject(policyId);
        verify(policyUseCase, never()).approve(any());
    }

    @Test
    void shouldThrowWhenDeserializationFailsSubscription() throws Exception {
        String payload = "INVALID_JSON";

        when(objectMapper.readValue(payload, MessageResult.class))
                .thenThrow(new JsonProcessingException("Erro de parsing!") {});

        assertThrows(JsonProcessingException.class,
                () -> listener.handleSubscriptionResult(payload));

        verifyNoInteractions(policyUseCase);
    }
}
