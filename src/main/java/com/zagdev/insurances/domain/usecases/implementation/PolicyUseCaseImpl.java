package com.zagdev.insurances.domain.usecases.implementation;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.services.PolicyService;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PolicyUseCaseImpl implements PolicyUseCase {

    private final PolicyService policyService;

    public PolicyUseCaseImpl(PolicyService policyService) {
        this.policyService = policyService;
    }

    @Override
    public PolicyDTO approve(UUID requestId) {
        return policyService.approve(requestId);
    }

    @Override
    public PolicyDTO cancel(UUID requestId) {
        return policyService.cancel(requestId);
    }

    @Override
    public PolicyDTO create(PolicyDTO request) {
        return policyService.create(request);
    }

    @Override
    public PolicyDTO findById(UUID id) {
        return policyService.findById(id);
    }

    @Override
    public List<PolicyDTO> findByCustomerId(UUID id) {
        return policyService.findByCustomerId(id);
    }

    @Override
    public PolicyDTO validate(UUID requestId) {
        return policyService.validate(requestId);
    }

    @Override
    public PolicyDTO reject(UUID requestId) {
        return policyService.reject(requestId);
    }
}
