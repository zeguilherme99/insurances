package com.zagdev.insurances.application.adapters.controllers;

import com.zagdev.insurances.application.adapters.dto.PolicyRequest;
import com.zagdev.insurances.application.adapters.dto.PolicyResponse;
import com.zagdev.insurances.application.adapters.mapper.PolicyMapper;
import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;
import com.zagdev.insurances.domain.usecases.PolicyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Policy", description = "Policy management API")
public class PolicyController {

    private final PolicyUseCase policyUseCase;

    private final Logger logger = LoggerFactory.getLogger(PolicyController.class);

    public PolicyController(PolicyUseCase policyUseCase) {
        this.policyUseCase = policyUseCase;
    }

    @Operation(
            summary = "Create policy",
            description = "Create new policy",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Policy created"),
                    @ApiResponse(responseCode = "400", description = "Invalid data"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            }
    )
    @PostMapping
    public ResponseEntity<PolicyResponse> create(@RequestBody PolicyRequest dto) throws InvalidDataException, UnexpectedErrorException {
        logger.info("Controller: Received request to create policy for customer [{}]", dto.getCustomerId());
        PolicyDTO policy = policyUseCase.create(PolicyMapper.toDomain(dto));
        logger.info("Controller: Policy created successfully for customer [{}] with id [{}] (status: [{}])",
                policy.getCustomerId(), policy.getId(), policy.getStatus());
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @Operation(
            summary = "Validate Policy",
            description = "Validate policy by ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Policy valitated"),
                    @ApiResponse(responseCode = "400", description = "Invalid Data"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            }
    )
    @PatchMapping("/{id}/validate")
    public ResponseEntity<PolicyResponse> validate(@Valid @PathVariable UUID id) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("Controller: Received request to validate policy [{}]", id);
        PolicyDTO policy = policyUseCase.validate(id);
        logger.info("Controller: Policy [{}] validated (status: [{}])", policy.getId(), policy.getStatus());
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @Operation(
            summary = "Cancel Policy",
            description = "Cancel policy by id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Policy cancelled"),
                    @ApiResponse(responseCode = "400", description = "Invalid Data"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            }
    )
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PolicyResponse> cancel(@PathVariable UUID id) throws DataNotFoundException, InvalidDataException, UnexpectedErrorException {
        logger.info("Controller: Received request to cancel policy [{}]", id);
        PolicyDTO policy = policyUseCase.cancel(id);
        logger.info("Controller: Policy [{}] cancelled (status: [{}])", policy.getId(), policy.getStatus());
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @Operation(
            summary = "Get policy by ID",
            description = "Get policy by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Policy found"),
                    @ApiResponse(responseCode = "400", description = "Invalid Data"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getById(@Valid @PathVariable UUID id) throws DataNotFoundException {
        logger.info("Controller: Received request to get policy [{}]", id);
        PolicyDTO policy = policyUseCase.findById(id);
        logger.info("Controller: Policy [{}] found (status: [{}])", policy.getId(), policy.getStatus());
        return ResponseEntity.ok(PolicyMapper.toResponse(policy));
    }

    @Operation(
            summary = "Get policies by customer",
            description = "Get policies by customer Id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Policies List found"),
            }
    )
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PolicyResponse>> getByCustomer(@PathVariable UUID customerId) {
        logger.info("Controller: Received request to get policies for customer [{}]", customerId);
        List<PolicyDTO> policies = policyUseCase.findByCustomerId(customerId);
        logger.info("Controller: Found [{}] policies for customer [{}]", policies.size(), customerId);
        List<PolicyResponse> response = policies.stream().map(PolicyMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
