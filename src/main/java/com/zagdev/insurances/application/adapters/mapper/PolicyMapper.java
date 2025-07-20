package com.zagdev.insurances.application.adapters.mapper;

import com.zagdev.insurances.application.adapters.dto.PolicyRequest;
import com.zagdev.insurances.application.adapters.dto.PolicyResponse;
import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.enums.InsuranceCategory;

import java.util.stream.Collectors;

public class PolicyMapper {

    public static PolicyDTO toDomain(PolicyRequest dto) {
        return new PolicyDTO(
                dto.customerId,
                dto.productId,
                InsuranceCategory.valueOf(dto.category),
                dto.salesChannel,
                dto.paymentMethod,
                dto.totalMonthlyPremiumAmount,
                dto.insuredAmount,
                dto.coverages,
                dto.assistances
        );
    }

    public static PolicyResponse toResponse(PolicyDTO policy) {
        PolicyResponse dto = new PolicyResponse();
        dto.id = policy.getId();
        dto.customerId = policy.getCustomerId();
        dto.productId = policy.getProductId();
        dto.category = policy.getCategory().name();
        dto.salesChannel = policy.getSalesChannel();
        dto.paymentMethod = policy.getPaymentMethod();
        dto.status = policy.getStatus();
        dto.createdAt = policy.getCreatedAt();
        dto.finishedAt = policy.getFinishedAt();
        dto.totalMonthlyPremiumAmount = policy.getTotalMonthlyPremium();
        dto.insuredAmount = policy.getInsuredAmount();
        dto.coverages = policy.getCoverages();
        dto.assistances = policy.getAssistances();

        dto.history = policy.getHistory().stream().map(history -> {
            PolicyResponse.StatusHistoryDTO h = new PolicyResponse.StatusHistoryDTO();
            h.status = history.getStatus();
            h.timestamp = history.getTimestamp();
            return h;
        }).collect(Collectors.toList());

        return dto;
    }
}
