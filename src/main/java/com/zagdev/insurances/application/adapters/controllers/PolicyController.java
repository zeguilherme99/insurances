package com.zagdev.insurances.application.adapters.controllers;

import com.zagdev.insurances.application.adapters.dto.PolicyRequest;
import com.zagdev.insurances.application.adapters.dto.PolicyResponse;
import com.zagdev.insurances.application.adapters.mapper.PolicyMapper;
import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyUseCase policyUseCase;

    public PolicyController(PolicyUseCase policyUseCase) {
        this.policyUseCase = policyUseCase;
    }

    @PostMapping
    public ResponseEntity<PolicyResponse> create(@RequestBody PolicyRequest dto) {
        PolicyDTO policy = policyUseCase.create(PolicyMapper.toDomain(dto));
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @PatchMapping("/{id}/validate")
    public ResponseEntity<PolicyResponse> validate(@Valid @PathVariable UUID id) throws DataNotFoundException, InvalidDataException {
        PolicyDTO policy = policyUseCase.validate(id);
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<PolicyResponse> approve(@PathVariable UUID id) throws DataNotFoundException, InvalidDataException {
        PolicyDTO policy = policyUseCase.approve(id);
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PolicyResponse> cancel(@PathVariable UUID id) throws DataNotFoundException, InvalidDataException {
        PolicyDTO policy = policyUseCase.cancel(id);
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getById(@Valid @PathVariable UUID id) throws DataNotFoundException {
        PolicyDTO policy = policyUseCase.findById(id);
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PolicyResponse>> getByCustomer(@PathVariable UUID customerId) {
        List<PolicyDTO> policies = policyUseCase.findByCustomerId(customerId);
        List<PolicyResponse> response = policies.stream().map(PolicyMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
