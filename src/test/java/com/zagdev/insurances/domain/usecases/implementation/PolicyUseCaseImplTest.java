package com.zagdev.insurances.domain.usecases.implementation;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import com.zagdev.insurances.domain.services.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyUseCaseImplTest {

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private PolicyUseCaseImpl useCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private PolicyDTO buildPolicyDTO(UUID id, PolicyStatus status) {
        PolicyDTO dto = new PolicyDTO();
        dto.setId(id);
        dto.setStatus(status);
        dto.setCustomerId(UUID.randomUUID());
        dto.setCategory(com.zagdev.insurances.domain.enums.InsuranceCategory.LIFE);
        dto.setInsuredAmount(BigDecimal.valueOf(123_456));
        dto.setCreatedAt(Instant.now().minusSeconds(1200));
        dto.setFinishedAt(null);
        return dto;
    }

    @Test
    void shouldDelegateApprove() {
        UUID id = UUID.randomUUID();
        PolicyDTO expected = buildPolicyDTO(id, PolicyStatus.APPROVED);
        when(policyService.approve(id)).thenReturn(expected);

        PolicyDTO result = useCase.approve(id);

        assertSame(expected, result);
        assertEquals(id, result.getId());
        assertEquals(PolicyStatus.APPROVED, result.getStatus());
        verify(policyService).approve(id);
    }

    @Test
    void shouldDelegateCancel() {
        UUID id = UUID.randomUUID();
        PolicyDTO expected = buildPolicyDTO(id, PolicyStatus.CANCELLED);
        when(policyService.cancel(id)).thenReturn(expected);

        PolicyDTO result = useCase.cancel(id);

        assertSame(expected, result);
        assertEquals(id, result.getId());
        assertEquals(PolicyStatus.CANCELLED, result.getStatus());
        verify(policyService).cancel(id);
    }

    @Test
    void shouldDelegateCreate() {
        PolicyDTO request = buildPolicyDTO(null, PolicyStatus.PENDING);
        PolicyDTO expected = buildPolicyDTO(UUID.randomUUID(), PolicyStatus.PENDING);
        when(policyService.create(request)).thenReturn(expected);

        PolicyDTO result = useCase.create(request);

        assertNotNull(result.getId());
        assertEquals(expected.getId(), result.getId());
        assertEquals(PolicyStatus.PENDING, result.getStatus());
        verify(policyService).create(request);
    }

    @Test
    void shouldDelegateFindById() {
        UUID id = UUID.randomUUID();
        PolicyDTO expected = buildPolicyDTO(id, PolicyStatus.VALIDATED);
        when(policyService.findById(id)).thenReturn(expected);

        PolicyDTO result = useCase.findById(id);

        assertEquals(id, result.getId());
        assertEquals(PolicyStatus.VALIDATED, result.getStatus());
        verify(policyService).findById(id);
    }

    @Test
    void shouldDelegateFindByCustomerId() {
        UUID customerId = UUID.randomUUID();
        PolicyDTO p1 = buildPolicyDTO(UUID.randomUUID(), PolicyStatus.APPROVED);
        PolicyDTO p2 = buildPolicyDTO(UUID.randomUUID(), PolicyStatus.PENDING);
        List<PolicyDTO> expected = List.of(p1, p2);

        when(policyService.findByCustomerId(customerId)).thenReturn(expected);

        List<PolicyDTO> result = useCase.findByCustomerId(customerId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getStatus() == PolicyStatus.APPROVED));
        assertTrue(result.stream().anyMatch(dto -> dto.getStatus() == PolicyStatus.PENDING));
        verify(policyService).findByCustomerId(customerId);
    }

    @Test
    void shouldDelegateValidate() {
        UUID id = UUID.randomUUID();
        PolicyDTO expected = buildPolicyDTO(id, PolicyStatus.VALIDATED);
        when(policyService.validate(id)).thenReturn(expected);

        PolicyDTO result = useCase.validate(id);

        assertEquals(id, result.getId());
        assertEquals(PolicyStatus.VALIDATED, result.getStatus());
        verify(policyService).validate(id);
    }

    @Test
    void shouldDelegateReject() {
        UUID id = UUID.randomUUID();
        PolicyDTO expected = buildPolicyDTO(id, PolicyStatus.REJECTED);
        when(policyService.reject(id)).thenReturn(expected);

        PolicyDTO result = useCase.reject(id);

        assertEquals(id, result.getId());
        assertEquals(PolicyStatus.REJECTED, result.getStatus());
        verify(policyService).reject(id);
    }
}

