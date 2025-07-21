package com.zagdev.insurances.domain.services.implementation;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.entity.Policy;
import com.zagdev.insurances.domain.enums.PolicyStatus;
import com.zagdev.insurances.domain.enums.RiskClassification;
import com.zagdev.insurances.domain.event.PolicyEvent;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.ErrorCode;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;
import com.zagdev.insurances.domain.mapper.PolicyMapper;
import com.zagdev.insurances.domain.repositories.PolicyMongoRepository;
import com.zagdev.insurances.domain.services.PolicyService;
import com.zagdev.insurances.infrastructure.EventPublisher;
import com.zagdev.insurances.infrastructure.FraudApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PolicyServiceImpl implements PolicyService {

    private static final Logger logger = LoggerFactory.getLogger(PolicyServiceImpl.class);

    private final PolicyMongoRepository repository;
    private final FraudApiClient fraudClient;
    private final EventPublisher eventPublisher;

    public PolicyServiceImpl(PolicyMongoRepository repository, FraudApiClient fraudClient, EventPublisher eventPublisher) {
        this.repository = repository;
        this.fraudClient = fraudClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PolicyDTO approve(UUID requestId) throws InvalidDataException, DataNotFoundException, UnexpectedErrorException {
        logger.info("Starting approval for policy [{}]", requestId);
        PolicyDTO request = getById(requestId);
        PolicyStatus previousStatus = request.getStatus();
        request.approve();

        logStatusTransition(requestId, previousStatus, request.getStatus());

        return saveAndPublish(request);
    }

    @Override
    public PolicyDTO cancel(UUID requestId) throws InvalidDataException, DataNotFoundException, UnexpectedErrorException {
        logger.info("Starting cancellation for policy [{}]", requestId);
        PolicyDTO request = getById(requestId);
        PolicyStatus previousStatus = request.getStatus();

        request.cancel();

        logStatusTransition(requestId, previousStatus, request.getStatus());

        return saveAndPublish(request);
    }

    @Override
    public PolicyDTO create(PolicyDTO request) throws InvalidDataException, UnexpectedErrorException {
        logger.info("Service: Creating new policy for customer [{}]", request.getCustomerId());
        PolicyDTO result = saveAndPublish(request);
        logger.info("Service: Policy created with id [{}] for customer [{}] (status: [{}])",
                result.getId(), result.getCustomerId(), result.getStatus());
        return result;
    }

    @Override
    public PolicyDTO findById(UUID id) throws DataNotFoundException {
        return getById(id);
    }

    @Override
    public List<PolicyDTO> findByCustomerId(UUID id) {
        logger.info("Service: Finding policies for customer [{}]", id);
        List<PolicyDTO> list = repository.findByCustomerId(id).stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());
        logger.info("Service: Found [{}] policies for customer [{}]", list.size(), id);
        return list;
    }

    @Override
    public PolicyDTO validate(UUID requestId) throws InvalidDataException, DataNotFoundException, UnexpectedErrorException {
        logger.info("Service: Validating policy [{}]", requestId);
        PolicyDTO request = getById(requestId);
        PolicyStatus previousStatus = request.getStatus();

        RiskClassification classification = fraudClient.getRiskClassification(request.getId(), request.getCustomerId());

        request.validate(classification);

        logStatusTransition(requestId, previousStatus, request.getStatus());

        if (request.getStatus() == PolicyStatus.VALIDATED) {
            previousStatus = request.getStatus();
            publishEvent(request);
            request.markAsPending();
            logStatusTransition(requestId, previousStatus, request.getStatus());
        }

        logger.info("Service: Policy [{}] validation process finished with status [{}]", requestId, request.getStatus());

        return saveAndPublish(request);
    }

    @Override
    public PolicyDTO reject(UUID requestId) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("Rejecting policy [{}]", requestId);
        PolicyDTO request = getById(requestId);
        PolicyStatus previousStatus = request.getStatus();

        request.reject();

        logStatusTransition(requestId, previousStatus, request.getStatus());
        return saveAndPublish(request);
    }

    @Override
    public PolicyDTO setPaymentConfirmed(UUID requestId) throws DataNotFoundException {
        logger.info("Service: Confirming payment for policy [{}]", requestId);
        PolicyDTO policyDTO = getById(requestId);
        policyDTO.setPaymentConfirmed(true);
        PolicyDTO updatedDTO = save(policyDTO);
        logger.info("Service: Payment confirmed for policy [{}]", requestId);
        return updatedDTO;
    }

    @Override
    public PolicyDTO setSubscriptionAuthorized(UUID requestId) throws DataNotFoundException {
        logger.info("Service: Setting subscription authorized for policy [{}]", requestId);
        PolicyDTO policyDTO = getById(requestId);
        policyDTO.setSubscriptionAuthorized(true);
        PolicyDTO updatedDTO = save(policyDTO);
        logger.info("Service: Subscription authorized for policy [{}]", requestId);
        return updatedDTO;
    }

    private PolicyDTO getById(UUID id) throws DataNotFoundException {
        logger.info("Service: Finding policy by id [{}]", id);
        Policy policy = repository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.POLICY_NOT_FOUND));

        logger.info("Service: Found policy with id [{}] (status: [{}])", policy.getId(), policy.getStatus());
        return PolicyMapper.toDomain(policy);
    }

    private PolicyDTO saveAndPublish(PolicyDTO request) throws InvalidDataException, UnexpectedErrorException {
        publishEvent(request);
        Policy policy = repository.save(PolicyMapper.toDocument(request));
        return PolicyMapper.toDomain(policy);
    }

    private PolicyDTO save(PolicyDTO request) {
        Policy policy = repository.save(PolicyMapper.toDocument(request));
        return PolicyMapper.toDomain(policy);
    }

    private void logStatusTransition(UUID policyId, PolicyStatus from, PolicyStatus to) {
        if (from != to) {
            logger.info("Service: Policy [{}] status changed from [{}] to [{}]", policyId, from, to);
        }
    }

    private void publishEvent(PolicyDTO policy) throws InvalidDataException, UnexpectedErrorException {
        eventPublisher.publish(new PolicyEvent(
                policy.getId(),
                policy.getCustomerId(),
                policy.getStatus()
        ));
    }
}
