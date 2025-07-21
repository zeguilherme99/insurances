package com.zagdev.insurances.domain.usecases.implementation;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;
import com.zagdev.insurances.domain.services.PolicyService;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PolicyUseCaseImpl implements PolicyUseCase {

    private static final Logger logger = LoggerFactory.getLogger(PolicyUseCaseImpl.class);

    private final PolicyService policyService;

    public PolicyUseCaseImpl(PolicyService policyService) {
        this.policyService = policyService;
    }

    @Override
    public PolicyDTO approve(UUID requestId) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("UseCase: Requested to approve policy [{}]", requestId);
        PolicyDTO dto = policyService.approve(requestId);
        logger.info("UseCase: Policy [{}] approved successfully (status: [{}])", dto.getId(), dto.getStatus());
        return dto;
    }

    @Override
    public PolicyDTO cancel(UUID requestId) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("UseCase: Requested to cancel policy [{}]", requestId);
        PolicyDTO dto = policyService.cancel(requestId);
        logger.info("UseCase: Policy [{}] cancelled successfully (status: [{}])", dto.getId(), dto.getStatus());
        return dto;
    }

    @Override
    public PolicyDTO create(PolicyDTO request) throws InvalidDataException, UnexpectedErrorException {
        logger.info("UseCase: Requested to create new policy for customer [{}]", request.getCustomerId());
        PolicyDTO dto = policyService.create(request);
        logger.info("UseCase: Policy created successfully with id [{}] for customer [{}] (status: [{}])",
                dto.getId(), dto.getCustomerId(), dto.getStatus());
        return dto;
    }

    @Override
    public PolicyDTO findById(UUID id) throws DataNotFoundException {
        logger.info("UseCase: Requested to find policy by id [{}]", id);
        PolicyDTO dto = policyService.findById(id);
        logger.info("UseCase: Policy found with id [{}] (status: [{}])", dto.getId(), dto.getStatus());
        return dto;
    }

    @Override
    public List<PolicyDTO> findByCustomerId(UUID id) {
        logger.info("UseCase: Requested to find policies by customer id [{}]", id);
        List<PolicyDTO> list = policyService.findByCustomerId(id);
        logger.info("UseCase: Found [{}] policies for customer [{}]", list.size(), id);
        return list;
    }

    @Override
    public PolicyDTO validate(UUID requestId) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("UseCase: Requested to validate policy [{}]", requestId);
        PolicyDTO dto = policyService.validate(requestId);
        logger.info("UseCase: Policy [{}] validated successfully (status: [{}])", dto.getId(), dto.getStatus());
        return dto;
    }

    @Override
    public PolicyDTO reject(UUID requestId) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("UseCase: Requested to reject policy [{}]", requestId);
        PolicyDTO dto = policyService.reject(requestId);
        logger.info("UseCase: Policy [{}] rejected successfully (status: [{}])", dto.getId(), dto.getStatus());
        return dto;
    }
}
