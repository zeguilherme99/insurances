package com.zagdev.insurances.application.adapters.controllers;

import com.zagdev.insurances.application.adapters.dto.PolicyRequest;
import com.zagdev.insurances.application.adapters.dto.PolicyResponse;
import com.zagdev.insurances.application.adapters.mapper.PolicyMapper;
import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyUseCase policyUseCase;

    private final Logger logger = LoggerFactory.getLogger(PolicyController.class);

    public PolicyController(PolicyUseCase policyUseCase) {
        this.policyUseCase = policyUseCase;
    }

    @PostMapping
    public ResponseEntity<PolicyResponse> create(@RequestBody PolicyRequest dto) throws InvalidDataException, UnexpectedErrorException {
        logger.info("Controller: Received request to create policy for customer [{}]", dto.getCustomerId());
        PolicyDTO policy = policyUseCase.create(PolicyMapper.toDomain(dto));
        logger.info("Controller: Policy created successfully for customer [{}] with id [{}] (status: [{}])",
                policy.getCustomerId(), policy.getId(), policy.getStatus());
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @PatchMapping("/{id}/validate")
    public ResponseEntity<PolicyResponse> validate(@Valid @PathVariable UUID id) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("Controller: Received request to validate policy [{}]", id);
        PolicyDTO policy = policyUseCase.validate(id);
        logger.info("Controller: Policy [{}] validated (status: [{}])", policy.getId(), policy.getStatus());
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PolicyResponse> cancel(@PathVariable UUID id) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("Controller: Received request to cancel policy [{}]", id);
        PolicyDTO policy = policyUseCase.cancel(id);
        logger.info("Controller: Policy [{}] cancelled (status: [{}])", policy.getId(), policy.getStatus());
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getById(@Valid @PathVariable UUID id) throws DataNotFoundException {
        logger.info("Controller: Received request to get policy [{}]", id);
        PolicyDTO policy = policyUseCase.findById(id);
        logger.info("Controller: Policy [{}] found (status: [{}])", policy.getId(), policy.getStatus());
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PolicyResponse>> getByCustomer(@PathVariable UUID customerId) {
        logger.info("Controller: Received request to get policies for customer [{}]", customerId);
        List<PolicyDTO> policies = policyUseCase.findByCustomerId(customerId);
        logger.info("Controller: Found [{}] policies for customer [{}]", policies.size(), customerId);
        List<PolicyResponse> response = policies.stream().map(PolicyMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
