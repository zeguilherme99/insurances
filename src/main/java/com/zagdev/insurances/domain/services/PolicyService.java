package com.zagdev.insurances.domain.services;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;

import java.util.List;
import java.util.UUID;

public interface PolicyService {

    PolicyDTO approve(UUID requestId) throws InvalidDataException, DataNotFoundException, UnexpectedErrorException;
    PolicyDTO cancel(UUID requestId) throws InvalidDataException, DataNotFoundException, UnexpectedErrorException;
    PolicyDTO create(PolicyDTO request) throws InvalidDataException, UnexpectedErrorException;
    PolicyDTO findById(UUID id) throws DataNotFoundException;
    List<PolicyDTO> findByCustomerId(UUID id);
    PolicyDTO validate(UUID requestId) throws InvalidDataException, DataNotFoundException, UnexpectedErrorException;
    PolicyDTO reject(UUID requestId) throws InvalidDataException, DataNotFoundException, UnexpectedErrorException;
    PolicyDTO setPaymentConfirmed(UUID requestId) throws DataNotFoundException;
    PolicyDTO setSubscriptionAuthorized(UUID requestId) throws DataNotFoundException;
}
