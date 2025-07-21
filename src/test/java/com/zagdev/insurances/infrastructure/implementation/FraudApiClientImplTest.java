package com.zagdev.insurances.infrastructure.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import com.zagdev.insurances.domain.enums.RiskClassification;
import com.zagdev.insurances.domain.exceptions.ErrorCode;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.infrastructure.dto.FraudAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;

class FraudApiClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    private FraudApiClientImpl fraudApiClient;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        fraudApiClient = new FraudApiClientImpl(restTemplate);
    }

    @Test
    void shouldReturnClassificationWhenSuccess() throws InvalidDataException {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String expectedUrl = String.format("http://localhost:8081/frauds/%s/customers/%s", orderId, customerId);

        FraudAnalysisResponse response = new FraudAnalysisResponse();
        response.setClassification(RiskClassification.REGULAR);

        when(restTemplate.getForObject(expectedUrl, FraudAnalysisResponse.class)).thenReturn(response);

        RiskClassification result = fraudApiClient.getRiskClassification(orderId, customerId);

        assertEquals(RiskClassification.REGULAR, result);
        verify(restTemplate).getForObject(expectedUrl, FraudAnalysisResponse.class);
    }

    @Test
    void shouldThrowWhenResponseIsNull() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String expectedUrl = String.format("http://localhost:8081/frauds/%s/customers/%s", orderId, customerId);

        when(restTemplate.getForObject(expectedUrl, FraudAnalysisResponse.class)).thenReturn(null);

        Exception ex = assertThrows(InvalidDataException.class, () -> fraudApiClient.getRiskClassification(orderId, customerId));
        assertEquals(ex.getMessage(), ErrorCode.INVALID_DATA.getMessage());
    }

    @Test
    void shouldThrowWhenClassificationIsNull() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String expectedUrl = String.format("http://localhost:8081/frauds/%s/customers/%s", orderId, customerId);

        FraudAnalysisResponse response = new FraudAnalysisResponse();

        when(restTemplate.getForObject(expectedUrl, FraudAnalysisResponse.class)).thenReturn(response);

        Exception ex = assertThrows(InvalidDataException.class, () -> fraudApiClient.getRiskClassification(orderId, customerId));
        assertEquals(ex.getMessage(), ErrorCode.INVALID_DATA.getMessage());
    }
}
