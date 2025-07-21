package com.zagdev.insurances.domain.usecases;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;

import java.util.List;
import java.util.UUID;

public interface PolicyUseCase {

    PolicyDTO approve(UUID requestId) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException;
    PolicyDTO cancel(UUID requestId) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException;
    PolicyDTO create(PolicyDTO request) throws InvalidDataException, UnexpectedErrorException;
    PolicyDTO findById(UUID id) throws DataNotFoundException;
    List<PolicyDTO> findByCustomerId(UUID id);
    PolicyDTO validate(UUID requestId) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException;
    PolicyDTO reject(UUID requestId) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException;
}
