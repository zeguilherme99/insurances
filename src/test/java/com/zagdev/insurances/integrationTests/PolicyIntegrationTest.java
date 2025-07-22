package com.zagdev.insurances.integrationTests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.zagdev.insurances.application.adapters.dto.PolicyRequest;
import com.zagdev.insurances.application.adapters.dto.PolicyResponse;
import com.zagdev.insurances.domain.enums.InsuranceCategory;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import com.zagdev.insurances.domain.enums.RiskClassification;
import com.zagdev.insurances.domain.event.PolicyEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WireMockTest(httpPort = 18081)
class PolicyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private PolicyRequest buildRequest() {
        PolicyRequest req = new PolicyRequest();
        req.setCustomerId(UUID.randomUUID());
        req.setCategory("LIFE");
        req.setInsuredAmount(BigDecimal.valueOf(10000));
        return req;
    }

    @AfterEach
    void tearDown() {
        while (rabbitTemplate.receive("policy-status-queue") != null) {
            //clear qeue
        }
    }

    @Test
    void createAndGetPolicy() throws Exception {
        PolicyRequest request = buildRequest();
        String json = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse created = objectMapper.readValue(response, PolicyResponse.class);

        mockMvc.perform(get("/api/v1/policies/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId().toString()))
                .andExpect(jsonPath("$.customer_id").value(created.getCustomerId().toString()))
                .andExpect(jsonPath("$.category").value(created.getCategory()))
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        String message = (String) rabbitTemplate.receiveAndConvert("policy-status-queue", 2000);
        PolicyEvent event = objectMapper.readValue(message, PolicyEvent.class);

        assertEquals(event.getNewStatus(), PolicyStatus.RECEIVED);
        assertEquals(event.getPolicyId(), created.getId());
        assertEquals(event.getCustomerId(), created.getCustomerId());
    }

    @Test
    void findByCustomerId() throws Exception {
        PolicyRequest request = buildRequest();
        String json = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse created = objectMapper.readValue(response, PolicyResponse.class);

        mockMvc.perform(get("/api/v1/policies/customer/" + created.getCustomerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(created.getId().toString()));
    }

    @Test
    void validatePolicyShouldSetStatusValidatedAndPending() throws Exception {
        PolicyRequest request = buildRequest();
        String json = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse created = objectMapper.readValue(response, PolicyResponse.class);

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathMatching("/frauds/.*?/customers/.*"))
                .willReturn(okJson("{ \"classification\": \"REGULAR\" }")));

        String validateResponse = mockMvc.perform(patch("/api/v1/policies/" + created.getId() + "/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        PolicyResponse validated = objectMapper.readValue(validateResponse, PolicyResponse.class);

        assertEquals(3, validated.getHistory().size());
        assertTrue(validated.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.VALIDATED));
        assertTrue(validated.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.PENDING));
        assertTrue(validated.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.RECEIVED));

        verify(getRequestedFor(urlPathMatching("/frauds/.*/customers/.*")));
    }

    private static Stream<TestCase> provideValidationScenarios() {
        return Stream.of(
                new TestCase(RiskClassification.REGULAR, InsuranceCategory.LIFE, new BigDecimal(10_000), true),
                new TestCase(RiskClassification.REGULAR, InsuranceCategory.LIFE, new BigDecimal(510_000), false),
                new TestCase(RiskClassification.REGULAR, InsuranceCategory.AUTO, new BigDecimal(349_000), true),
                new TestCase(RiskClassification.REGULAR, InsuranceCategory.AUTO, new BigDecimal(351_000), false),
                new TestCase(RiskClassification.REGULAR, InsuranceCategory.OTHER, new BigDecimal(254_000), true),
                new TestCase(RiskClassification.REGULAR, InsuranceCategory.OTHER, new BigDecimal(256_000), false),
                new TestCase(RiskClassification.HIGH_RISK, InsuranceCategory.AUTO, new BigDecimal(200_000), true),
                new TestCase(RiskClassification.HIGH_RISK, InsuranceCategory.AUTO, new BigDecimal(251_000), false),
                new TestCase(RiskClassification.HIGH_RISK, InsuranceCategory.RESIDENTIAL, new BigDecimal(149_000), true),
                new TestCase(RiskClassification.HIGH_RISK, InsuranceCategory.RESIDENTIAL, new BigDecimal(151_000), false),
                new TestCase(RiskClassification.HIGH_RISK, InsuranceCategory.OTHER, new BigDecimal(124_000), true),
                new TestCase(RiskClassification.HIGH_RISK, InsuranceCategory.OTHER, new BigDecimal(126_000), false),
                new TestCase(RiskClassification.PREFERENTIAL, InsuranceCategory.LIFE, new BigDecimal(800_000), true),
                new TestCase(RiskClassification.PREFERENTIAL, InsuranceCategory.LIFE, new BigDecimal(801_000), false),
                new TestCase(RiskClassification.NO_INFORMATION, InsuranceCategory.AUTO, new BigDecimal(75_000), true),
                new TestCase(RiskClassification.NO_INFORMATION, InsuranceCategory.AUTO, new BigDecimal(76_000), false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidationScenarios")
    void validatePolicy_ShouldHandleAllScenarios(TestCase testCase) throws Exception {
        PolicyRequest request = new PolicyRequest();
        request.setCustomerId(UUID.randomUUID());
        request.setCategory(testCase.category.name());
        request.setInsuredAmount(testCase.amount);

        String json = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse created = objectMapper.readValue(response, PolicyResponse.class);

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathMatching("/frauds/.*?/customers/.*"))
                .willReturn(okJson("{ \"classification\": \"" + testCase.classification.name() + "\" }")));

        String validateResponse = mockMvc.perform(patch("/api/v1/policies/" + created.getId() + "/validate"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse validated = objectMapper.readValue(validateResponse, PolicyResponse.class);

        PolicyStatus expectedStatus = testCase.shouldBeApproved ? PolicyStatus.PENDING : PolicyStatus.REJECTED;
        assertEquals(expectedStatus, validated.getStatus());

        List<PolicyEvent> policyEvents = getPolicyRabbitMqEvents();

        assertTrue(validated.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.RECEIVED));
        if (testCase.shouldBeApproved) {
            assertEquals(3, validated.getHistory().size());
            assertEquals(3, policyEvents.size());
            assertTrue(validated.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.VALIDATED));
            assertTrue(validated.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.PENDING));
        } else {
            assertEquals(2, validated.getHistory().size());
            assertEquals(2, policyEvents.size());
            assertTrue(validated.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.REJECTED));
        }
    }

    @Test
    void policyCancelScenario() throws Exception {
        PolicyRequest request = buildRequest();
        String json = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse created = objectMapper.readValue(response, PolicyResponse.class);

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathMatching("/frauds/.*?/customers/.*"))
                .willReturn(okJson("{ \"classification\": \"REGULAR\" }")));

        mockMvc.perform(patch("/api/v1/policies/" + created.getId() + "/validate"))
                .andExpect(status().isOk());

        String cancelResponse = mockMvc.perform(patch("/api/v1/policies/" + created.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse cancelled = objectMapper.readValue(cancelResponse, PolicyResponse.class);

        assertEquals(PolicyStatus.CANCELLED, cancelled.getStatus());
        assertTrue(cancelled.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.CANCELLED));
        assertTrue(cancelled.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.RECEIVED));
        assertTrue(cancelled.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.VALIDATED));
        assertTrue(cancelled.getHistory().stream().anyMatch(h -> h.getStatus() == PolicyStatus.PENDING));

        List<PolicyEvent> policyEvents = getPolicyRabbitMqEvents();
        assertEquals(4, cancelled.getHistory().size());
        assertEquals(4, policyEvents.size());
        assertTrue(policyEvents.stream().anyMatch(e -> e.getNewStatus() == PolicyStatus.CANCELLED));
    }

    @Test
    void shouldNotCancelAlreadyCancelledPolicy() throws Exception {
        PolicyRequest request = buildRequest();
        String json = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PolicyResponse created = objectMapper.readValue(response, PolicyResponse.class);

        mockMvc.perform(patch("/api/v1/policies/" + created.getId() + "/cancel")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/policies/" + created.getId() + "/cancel"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForNonExistentPolicy() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/policies/" + randomId))
                .andExpect(status().isNotFound());
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

    private record TestCase(RiskClassification classification, InsuranceCategory category, BigDecimal amount,
                            boolean shouldBeApproved) {
    }
}
