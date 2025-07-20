package com.zagdev.insurances.domain.mapper;

import com.zagdev.insurances.domain.dto.PolicyDTO;
import com.zagdev.insurances.domain.entity.Policy;
import com.zagdev.insurances.domain.dto.StatusChange;

import java.util.stream.Collectors;

public class PolicyMapper {

    public static Policy toDocument(PolicyDTO domain) {
        var doc = new Policy();
        doc.setId(domain.getId());
        doc.setCustomerId(domain.getCustomerId());
        doc.setProductId(domain.getProductId());
        doc.setCategory(domain.getCategory());
        doc.setSalesChannel(domain.getSalesChannel());
        doc.setPaymentMethod(domain.getPaymentMethod());
        doc.setTotalMonthlyPremium(domain.getTotalMonthlyPremium());
        doc.setInsuredAmount(domain.getInsuredAmount());
        doc.setCoverages(domain.getCoverages());
        doc.setAssistances(domain.getAssistances());
        doc.setStatus(domain.getStatus());
        doc.setCreatedAt(domain.getCreatedAt());
        doc.setFinishedAt(domain.getFinishedAt());

        var history = domain.getHistory().stream().map(h -> {
            var hDoc = new Policy.StatusHistory();
            hDoc.setStatus(h.getStatus());
            hDoc.setTimestamp(h.getTimestamp());
            return hDoc;
        }).collect(Collectors.toList());

        doc.setHistory(history);
        return doc;
    }

    public static PolicyDTO toDomain(Policy doc) {
        var domain = new PolicyDTO(
                doc.getCustomerId(),
                doc.getProductId(),
                doc.getCategory(),
                doc.getSalesChannel(),
                doc.getPaymentMethod(),
                doc.getTotalMonthlyPremium(),
                doc.getInsuredAmount(),
                doc.getCoverages(),
                doc.getAssistances()
        );

        domain.setId(doc.getId());
        domain.setStatus(doc.getStatus());
        domain.setCreatedAt(doc.getCreatedAt());
        domain.setFinishedAt(doc.getFinishedAt());

        var history = doc.getHistory().stream().map(h ->
                new StatusChange(h.getStatus(), h.getTimestamp())
        ).toList();

        domain.getHistory().clear();
        domain.getHistory().addAll(history);

        return domain;
    }
}
