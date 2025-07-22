package com.zagdev.insurances.integrationTests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.zagdev.insurances.application.adapters.dto.PolicyRequest;
import com.zagdev.insurances.application.adapters.dto.PolicyResponse;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import com.zagdev.insurances.domain.event.PolicyEvent;
import com.zagdev.insurances.infrastructure.dto.MessageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WireMockTest(httpPort = 18081)
class PolicyRabbitListenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @AfterEach
    void tearDown() {
        while (rabbitTemplate.receive("policy-status-queue") != null) {
        }
        while (rabbitTemplate.receive("payment-result-queue") != null) {
        }
        while (rabbitTemplate.receive("subscription-result-queue") != null) {
        }
    }

    static Stream<PaymentScenario> providePaymentScenarios() {
        return Stream.of(
                new PaymentScenario(true, PolicyStatus.APPROVED, true),
                new PaymentScenario(false, PolicyStatus.REJECTED, true)
        );
    }

    @ParameterizedTest
    @MethodSource("providePaymentScenarios")
    void paymentResultShouldUpdatePolicyStatus(PaymentScenario scenario) throws Exception {
        PolicyRequest request = new PolicyRequest();
        request.setCustomerId(UUID.randomUUID());
        request.setCategory("LIFE");
        request.setInsuredAmount(BigDecimal.valueOf(10000));

        String json = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse created = objectMapper.readValue(response, PolicyResponse.class);

        stubFor(get(urlPathMatching("/frauds/.*?/customers/.*"))
                .willReturn(okJson("{ \"classification\": \"REGULAR\" }")));

        mockMvc.perform(patch("/api/v1/policies/" + created.getId() + "/validate"))
                .andExpect(status().isOk());

        MessageResult messageResult = new MessageResult();
        messageResult.setPolicyId(created.getId());
        messageResult.setSuccess(scenario.paymentSuccess);

        String messageJson = objectMapper.writeValueAsString(messageResult);
        rabbitTemplate.convertAndSend("payment-result-queue", messageJson);
        Thread.sleep(1000);

        rabbitTemplate.convertAndSend("subscription-result-queue", messageJson);

        Thread.sleep(1000);

        String getResponse = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/policies/" + created.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse updated = objectMapper.readValue(getResponse, PolicyResponse.class);
        assertEquals(scenario.expectedStatus, updated.getStatus());

        List<PolicyEvent> policyEvents = getPolicyRabbitMqEvents();

        assertEquals(scenario.expectedStatus, updated.getStatus());
        assertEquals(4, updated.getHistory().size());
        assertEquals(4, policyEvents.size());
        assertTrue(updated.getHistory().stream().anyMatch(h -> h.getStatus() == scenario.expectedStatus));
    }

    private List<PolicyEvent> getPolicyRabbitMqEvents() throws JsonProcessingException {
        List<PolicyEvent> policyEvents = new ArrayList<>();

        String msg;
        while ((msg = (String) rabbitTemplate.receiveAndConvert("policy-status-queue", 1000)) != null) {
            PolicyEvent event = objectMapper.readValue(msg, PolicyEvent.class);
            policyEvents.add(event);
        }

        return policyEvents;
    }

    private record PaymentScenario(boolean paymentSuccess, PolicyStatus expectedStatus,
                                   boolean subscriptionAuthorized) {
    }
}

