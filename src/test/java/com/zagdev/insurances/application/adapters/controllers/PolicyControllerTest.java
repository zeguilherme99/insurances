package com.zagdev.insurances.application.adapters.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zagdev.insurances.application.adapters.dto.PolicyRequest;
import com.zagdev.insurances.application.adapters.mapper.PolicyMapper;
import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.enums.InsuranceCategory;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PolicyControllerTest {

    @Mock
    private PolicyUseCase policyUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @InjectMocks
    private PolicyController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    private PolicyRequest buildPolicyRequest() {
        PolicyRequest req = new PolicyRequest();
        req.setCustomerId(UUID.randomUUID());
        req.setCategory("LIFE");
        req.setInsuredAmount(BigDecimal.valueOf(9999));
        return req;
    }

    private PolicyDTO buildPolicyDTO(UUID id, PolicyStatus status) {
        PolicyDTO dto = new PolicyDTO();
        dto.setId(id);
        dto.setStatus(status);
        dto.setCustomerId(UUID.randomUUID());
        dto.setCategory(InsuranceCategory.LIFE);
        dto.setInsuredAmount(BigDecimal.valueOf(9999));
        dto.setCreatedAt(Instant.now().minusSeconds(60));
        dto.setFinishedAt(null);
        return dto;
    }

    @Test
    void shouldCreatePolicy() throws Exception {
        PolicyRequest request = buildPolicyRequest();
        PolicyDTO dto = PolicyMapper.toDomain(request);
        dto.setId(UUID.randomUUID());
        dto.setStatus(PolicyStatus.PENDING);

        when(policyUseCase.create(any(PolicyDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldValidatePolicy() throws Exception {
        UUID id = UUID.randomUUID();
        PolicyDTO dto = buildPolicyDTO(id, PolicyStatus.VALIDATED);

        when(policyUseCase.validate(id)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/policies/" + id + "/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
    void shouldApprovePolicy() throws Exception {
        UUID id = UUID.randomUUID();
        PolicyDTO dto = buildPolicyDTO(id, PolicyStatus.APPROVED);

        when(policyUseCase.approve(id)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/policies/" + id + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldCancelPolicy() throws Exception {
        UUID id = UUID.randomUUID();
        PolicyDTO dto = buildPolicyDTO(id, PolicyStatus.CANCELLED);

        when(policyUseCase.cancel(id)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/policies/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturnPolicyById() throws Exception {
        UUID id = UUID.randomUUID();
        PolicyDTO dto = buildPolicyDTO(id, PolicyStatus.PENDING);

        when(policyUseCase.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/policies/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnPoliciesByCustomerId() throws Exception {
        UUID customerId = UUID.randomUUID();
        PolicyDTO dto1 = buildPolicyDTO(UUID.randomUUID(), PolicyStatus.APPROVED);
        PolicyDTO dto2 = buildPolicyDTO(UUID.randomUUID(), PolicyStatus.PENDING);

        List<PolicyDTO> dtoList = List.of(dto1, dto2);

        when(policyUseCase.findByCustomerId(customerId)).thenReturn(dtoList);

        mockMvc.perform(get("/api/v1/policies/customer/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto1.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId().toString()));
    }
}
