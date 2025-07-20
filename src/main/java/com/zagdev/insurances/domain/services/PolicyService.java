package com.zagdev.insurances.domain.services;

import com.zagdev.insurances.domain.dto.PolicyDTO;

import java.util.List;
import java.util.UUID;

public interface PolicyService {

    PolicyDTO approve(UUID requestId);
    PolicyDTO cancel(UUID requestId);
    PolicyDTO create(PolicyDTO request);
    PolicyDTO findById(UUID id);
    List<PolicyDTO> findByCustomerId(UUID id);
    PolicyDTO validate(UUID requestId);
    PolicyDTO reject(UUID requestId);
}
