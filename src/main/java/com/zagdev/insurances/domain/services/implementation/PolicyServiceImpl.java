package com.zagdev.insurances.domain.services.implementation;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.entity.Policy;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import com.zagdev.insurances.domain.enums.RiskClassification;
import com.zagdev.insurances.domain.event.PolicyEvent;
import com.zagdev.insurances.domain.mapper.PolicyMapper;
import com.zagdev.insurances.domain.repositories.PolicyMongoRepository;
import com.zagdev.insurances.domain.services.PolicyService;
import com.zagdev.insurances.infrastructure.EventPublisher;
import com.zagdev.insurances.infrastructure.FraudApiClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PolicyServiceImpl implements PolicyService {

    private final PolicyMongoRepository repository;
    private final FraudApiClient fraudClient;
    private final EventPublisher eventPublisher;

    public PolicyServiceImpl(PolicyMongoRepository repository, FraudApiClient fraudClient, EventPublisher eventPublisher) {
        this.repository = repository;
        this.fraudClient = fraudClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PolicyDTO approve(UUID requestId) {
        PolicyDTO request = getById(requestId);

        request.approve();

        return save(request);
    }

    @Override
    public PolicyDTO cancel(UUID requestId) {
        PolicyDTO request = getById(requestId);

        request.cancel();

        return save(request);
    }

    @Override
    public PolicyDTO create(PolicyDTO request) {
        return save(request);
    }

    @Override
    public PolicyDTO findById(UUID id) {
        return getById(id);
    }

    @Override
    public List<PolicyDTO> findByCustomerId(UUID id) {
        return repository.findByCustomerId(id).stream().map(PolicyMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public PolicyDTO validate(UUID requestId) {
        PolicyDTO request = getById(requestId);

        RiskClassification classification = fraudClient.getRiskClassification(request.getId(), request.getCustomerId());

        request.validate(classification);

        if (request.getStatus() == PolicyStatus.VALIDATED) {
            eventPublisher.publish(new PolicyEvent(
                    request.getId(),
                    request.getCustomerId(),
                    request.getStatus()
            ));

            request.markAsPending();
        }

        return save(request);
    }

    @Override
    public PolicyDTO reject(UUID requestId) {
        PolicyDTO request = getById(requestId);

        request.reject();

        return save(request);
    }

    private PolicyDTO getById(UUID id) {
        Policy policy = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        return PolicyMapper.toDomain(policy);
    }

    private PolicyDTO save(PolicyDTO request) {
        Policy policy = repository.save(PolicyMapper.toDocument(request));

        publishEvent(policy);
        return PolicyMapper.toDomain(policy);
    }

    private void publishEvent(Policy policy) {
        eventPublisher.publish(new PolicyEvent(
                policy.getId(),
                policy.getCustomerId(),
                policy.getStatus()
        ));
    }
}
