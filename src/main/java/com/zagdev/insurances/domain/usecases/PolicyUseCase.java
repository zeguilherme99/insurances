package com.zagdev.insurances.domain.usecases;

import com.zagdev.insurances.domain.dto.PolicyDTO;

import java.util.List;
import java.util.UUID;

public interface PolicyUseCase {

    PolicyDTO approve(UUID requestId);
    PolicyDTO cancel(UUID requestId);
    PolicyDTO create(PolicyDTO request);
    PolicyDTO findById(UUID id);
    List<PolicyDTO> findByCustomerId(UUID id);
    PolicyDTO validate(UUID requestId);
    PolicyDTO reject(UUID requestId);
}
