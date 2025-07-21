package com.zagdev.insurances.infrastructure.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import com.zagdev.insurances.infrastructure.dto.PaymentResult;
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
    void shouldApproveWhenPaymentIsSuccess() throws Exception {
        UUID policyId = UUID.randomUUID();
        String payload = "{\"policyId\": \"" + policyId + "\", \"success\": true}";

        PaymentResult paymentResult = new PaymentResult();
        paymentResult.setPolicyId(policyId);
        paymentResult.setSuccess(true);

        when(objectMapper.readValue(payload, PaymentResult.class)).thenReturn(paymentResult);

        listener.handlePaymentResult(payload);

        verify(policyUseCase).approve(policyId);
        verify(policyUseCase, never()).reject(any());
    }

    @Test
    void shouldRejectWhenPaymentIsNotSuccess() throws Exception {
        UUID policyId = UUID.randomUUID();
        String payload = "{\"policyId\": \"" + policyId + "\", \"success\": false}";

        PaymentResult paymentResult = new PaymentResult();
        paymentResult.setPolicyId(policyId);
        paymentResult.setSuccess(false);

        when(objectMapper.readValue(payload, PaymentResult.class)).thenReturn(paymentResult);

        listener.handlePaymentResult(payload);

        verify(policyUseCase).reject(policyId);
        verify(policyUseCase, never()).approve(any());
    }

    @Test
    void shouldThrowWhenDeserializationFails() throws Exception {
        String payload = "INVALID_JSON";

        when(objectMapper.readValue(payload, PaymentResult.class))
                .thenThrow(new JsonProcessingException("Erro de parsing!") {});

        assertThrows(JsonProcessingException.class,
                () -> listener.handlePaymentResult(payload));

        verifyNoInteractions(policyUseCase);
    }
}
