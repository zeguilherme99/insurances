package com.zagdev.insurances.domain.services.implementation;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.entity.Policy;
import com.zagdev.insurances.domain.enums.InsuranceCategory;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import com.zagdev.insurances.domain.enums.RiskClassification;
import com.zagdev.insurances.domain.event.PolicyEvent;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.ErrorCode;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;
import com.zagdev.insurances.domain.repositories.PolicyMongoRepository;
import com.zagdev.insurances.infrastructure.EventPublisher;
import com.zagdev.insurances.infrastructure.FraudApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyServiceImplTest {

    @Mock
    private PolicyMongoRepository repository;
    @Mock
    private FraudApiClient fraudApiClient;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private PolicyServiceImpl service;

    private UUID policyId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        policyId = UUID.randomUUID();
    }

    private PolicyDTO buildPolicyDTO(UUID id, PolicyStatus status) {
        PolicyDTO dto = new PolicyDTO();
        dto.setId(id);
        dto.setCustomerId(id);
        dto.setStatus(status);
        dto.setCategory(InsuranceCategory.LIFE);
        dto.setInsuredAmount(BigDecimal.valueOf(100_000));
        return dto;
    }

    private Policy buildPolicy(UUID id, PolicyStatus status) {
        Policy policy = new Policy();
        policy.setId(id);
        policy.setCustomerId(id);
        policy.setStatus(status);
        policy.setCategory(InsuranceCategory.LIFE);
        policy.setInsuredAmount(BigDecimal.valueOf(100_000));
        policy.setHistory(List.of());
        return policy;
    }

    static class SuccessCase {
        String methodName;
        PolicyStatus initialStatus;
        PolicyStatus expectedStatus;

        public SuccessCase(String methodName, PolicyStatus initialStatus, PolicyStatus expectedStatus) {
            this.methodName = methodName;
            this.initialStatus = initialStatus;
            this.expectedStatus = expectedStatus;
        }
    }

    static class ErrorCase {
        String methodName;
        PolicyStatus initialStatus;

        public ErrorCase(String methodName, PolicyStatus initialStatus) {
            this.methodName = methodName;
            this.initialStatus = initialStatus;
        }
    }

    static Stream<SuccessCase> provideSuccessCases() {
        return Stream.of(
                new SuccessCase("approve", PolicyStatus.PENDING, PolicyStatus.APPROVED),
                new SuccessCase("cancel", PolicyStatus.PENDING, PolicyStatus.CANCELLED),
                new SuccessCase("reject", PolicyStatus.PENDING, PolicyStatus.REJECTED)
        );
    }

    static Stream<ErrorCase> provideErrorCases() {
        return Stream.of(
                new ErrorCase("approve", PolicyStatus.APPROVED),
                new ErrorCase("approve", PolicyStatus.REJECTED),
                new ErrorCase("cancel", PolicyStatus.CANCELLED),
                new ErrorCase("cancel", PolicyStatus.APPROVED),
                new ErrorCase("reject", PolicyStatus.APPROVED),
                new ErrorCase("reject", PolicyStatus.REJECTED),
                new ErrorCase("reject", PolicyStatus.CANCELLED),
                new ErrorCase("validate", PolicyStatus.CANCELLED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSuccessCases")
    void shouldTransitionPolicyStatusSuccessfully(SuccessCase testCase) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        Policy policy = buildPolicy(policyId, testCase.initialStatus);

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyDTO result = switch (testCase.methodName) {
            case "approve" -> service.approve(policyId);
            case "cancel"  -> service.cancel(policyId);
            case "reject"  -> service.reject(policyId);
            default -> throw new IllegalArgumentException("Método inválido para teste: " + testCase.methodName);
        };

        assertEquals(testCase.expectedStatus, result.getStatus());
        assertTrue(result.getHistory().stream().anyMatch(event -> event.getStatus() == testCase.expectedStatus));
    }

    @ParameterizedTest
    @MethodSource("provideErrorCases")
    void shouldThrowExceptionOnInvalidTransition(ErrorCase testCase) throws InvalidDataException, UnexpectedErrorException {
        Policy policy = buildPolicy(policyId, testCase.initialStatus);

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));

        Exception ex = assertThrows(InvalidDataException.class, () -> {
            switch (testCase.methodName) {
                case "approve" -> service.approve(policyId);
                case "cancel"  -> service.cancel(policyId);
                case "reject"  -> service.reject(policyId);
                case "validate"  -> service.validate(policyId);
                default -> throw new IllegalArgumentException("Método inválido para teste: " + testCase.methodName);
            }
        });

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
        assertEquals(ex.getMessage(), ErrorCode.INVALID_STATUS.getMessage());
    }

    @Test
    void shouldCreatePolicySuccessfully() throws InvalidDataException, UnexpectedErrorException {
        PolicyDTO dtoToSave = buildPolicyDTO(null, PolicyStatus.PENDING);
        Policy policySaved = buildPolicy(policyId, PolicyStatus.PENDING);

        when(repository.save(any())).thenReturn(policySaved);

        PolicyDTO result = service.create(dtoToSave);

        assertNotNull(result);
        assertEquals(policySaved.getId(), result.getId());
        assertEquals(PolicyStatus.PENDING, result.getStatus());
        verify(repository).save(any());
    }

    @Test
    void shouldFindPolicyByIdSuccessfully() throws DataNotFoundException {
        Policy policyFound = buildPolicy(policyId, PolicyStatus.APPROVED);

        when(repository.findById(policyId)).thenReturn(Optional.of(policyFound));

        PolicyDTO result = service.findById(policyId);

        assertNotNull(result);
        assertEquals(policyId, result.getId());
        assertEquals(PolicyStatus.APPROVED, result.getStatus());
        verify(repository).findById(policyId);
    }

    @Test
    void shouldThrowWhenPolicyNotFound() {
        when(repository.findById(policyId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.findById(policyId));
        verify(repository).findById(policyId);
    }

    @Test
    void shouldReturnPoliciesByCustomerId() {
        UUID customerId = UUID.randomUUID();

        Policy policy1 = buildPolicy(policyId, PolicyStatus.APPROVED);
        Policy policy2 = buildPolicy(UUID.randomUUID(), PolicyStatus.PENDING);

        when(repository.findByCustomerId(customerId)).thenReturn(List.of(policy1, policy2));

        List<PolicyDTO> result = service.findByCustomerId(customerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(policy1.getId())));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(policy2.getId())));
        verify(repository).findByCustomerId(customerId);
    }

    @Test
    void shouldReturnEmptyListWhenNoPolicyForCustomer() {
        UUID customerId = UUID.randomUUID();

        when(repository.findByCustomerId(customerId)).thenReturn(List.of());

        List<PolicyDTO> result = service.findByCustomerId(customerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findByCustomerId(customerId);
    }

    @Test
    void shouldValidatePolicyAndPublishEventsSuccessfully() throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        UUID requestId = UUID.randomUUID();
        PolicyDTO policyDto = buildPolicyDTO(requestId, PolicyStatus.PENDING);
        Policy policy = buildPolicy(requestId, PolicyStatus.PENDING);
        RiskClassification classification = RiskClassification.REGULAR;

        when(repository.findById(requestId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.getRiskClassification(any(), any())).thenReturn(classification);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyDTO result = service.validate(requestId);

        verify(repository).findById(requestId);
        verify(fraudApiClient).getRiskClassification(requestId, policyDto.getCustomerId());
        verify(eventPublisher, times(2)).publish(any(PolicyEvent.class));
        verify(repository).save(any());

        assertNotNull(result);
        assertEquals(result.getStatus(), PolicyStatus.PENDING);
        assertEquals(2, result.getHistory().size());
        assertTrue(result.getHistory().stream().anyMatch(event -> event.getStatus() == PolicyStatus.PENDING));
        assertTrue(result.getHistory().stream().anyMatch(event -> event.getStatus() == PolicyStatus.VALIDATED));
    }

    @Test
    void shouldValidateAndTransitionToRejected() throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        UUID requestId = UUID.randomUUID();
        Policy policy = buildPolicy(requestId, PolicyStatus.PENDING);
        RiskClassification classification = RiskClassification.HIGH_RISK;
        policy.setCategory(InsuranceCategory.LIFE);
        policy.setInsuredAmount(BigDecimal.valueOf(1_000_000));

        when(repository.findById(requestId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.getRiskClassification(any(), any())).thenReturn(classification);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(fraudApiClient.getRiskClassification(any(), any())).thenReturn(classification);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyDTO result = service.validate(requestId);

        assertEquals(PolicyStatus.REJECTED, result.getStatus());
        verify(eventPublisher).publish(any(PolicyEvent.class));
    }
}
