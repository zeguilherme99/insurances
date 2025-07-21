package com.zagdev.insurances.application.adapters.mapper;

import com.zagdev.insurances.application.adapters.dto.PolicyRequest;
import com.zagdev.insurances.application.adapters.dto.PolicyResponse;
import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.enums.InsuranceCategory;

import java.util.stream.Collectors;

public class PolicyMapper {

    public static PolicyDTO toDomain(PolicyRequest dto) {
        return new PolicyDTO(
                dto.getCustomerId(),
                dto.getProductId(),
                InsuranceCategory.valueOf(dto.getCategory()),
                dto.getSalesChannel(),
                dto.getPaymentMethod(),
                dto.getTotalMonthlyPremiumAmount(),
                dto.getInsuredAmount(),
                dto.getCoverages(),
                dto.getAssistances()
        );
    }

    public static PolicyResponse toResponse(PolicyDTO policy) {
        PolicyResponse dto = new PolicyResponse();
        dto.setId(policy.getId());
        dto.setCustomerId(policy.getCustomerId());
        dto.setProductId(policy.getProductId());
        dto.setCategory(policy.getCategory().name());
        dto.setSalesChannel(policy.getSalesChannel());
        dto.setPaymentMethod(policy.getPaymentMethod());
        dto.setStatus(policy.getStatus());
        dto.setCreatedAt(policy.getCreatedAt());
        dto.setFinishedAt(policy.getFinishedAt());
        dto.setTotalMonthlyPremiumAmount(policy.getTotalMonthlyPremium());
        dto.setInsuredAmount(policy.getInsuredAmount());
        dto.setCoverages(policy.getCoverages());
        dto.setAssistances(policy.getAssistances());

        dto.setHistory(policy.getHistory().stream().map(history -> {
            PolicyResponse.StatusHistoryDTO h = new PolicyResponse.StatusHistoryDTO();
            h.setStatus(history.getStatus());
            h.setTimestamp(history.getTimestamp());
            return h;
        }).collect(Collectors.toList()));

        return dto;
    }
}
