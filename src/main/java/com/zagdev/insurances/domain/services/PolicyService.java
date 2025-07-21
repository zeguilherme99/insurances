package com.zagdev.insurances.domain.services;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;

import java.util.List;
import java.util.UUID;

public interface PolicyService {

    PolicyDTO approve(UUID requestId) throws InvalidDataException, DataNotFoundException;
    PolicyDTO cancel(UUID requestId) throws InvalidDataException, DataNotFoundException;
    PolicyDTO create(PolicyDTO request);
    PolicyDTO findById(UUID id) throws DataNotFoundException;
    List<PolicyDTO> findByCustomerId(UUID id);
    PolicyDTO validate(UUID requestId) throws InvalidDataException, DataNotFoundException;
    PolicyDTO reject(UUID requestId) throws InvalidDataException, DataNotFoundException;
}
