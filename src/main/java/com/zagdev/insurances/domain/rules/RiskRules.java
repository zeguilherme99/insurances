package com.zagdev.insurances.domain.rules;

import com.zagdev.insurances.domain.enums.InsuranceCategory;
import com.zagdev.insurances.domain.enums.RiskClassification;

import java.math.BigDecimal;

public class RiskRules {

    public static boolean isApproved(RiskClassification classification, InsuranceCategory category, BigDecimal insuredAmount) {
        return switch (classification) {
            case REGULAR -> switch (category) {
                case LIFE, RESIDENTIAL -> insuredAmount.compareTo(BigDecimal.valueOf(500_000)) <= 0;
                case AUTO -> insuredAmount.compareTo(BigDecimal.valueOf(350_000)) <= 0;
                default -> insuredAmount.compareTo(BigDecimal.valueOf(255_000)) <= 0;
            };
            case HIGH_RISK -> switch (category) {
                case AUTO -> insuredAmount.compareTo(BigDecimal.valueOf(250_000)) <= 0;
                case RESIDENTIAL -> insuredAmount.compareTo(BigDecimal.valueOf(150_000)) <= 0;
                default -> insuredAmount.compareTo(BigDecimal.valueOf(125_000)) <= 0;
            };
            case PREFERENTIAL -> switch (category) {
                case LIFE -> insuredAmount.compareTo(BigDecimal.valueOf(800_000)) <= 0;
                case AUTO, RESIDENTIAL -> insuredAmount.compareTo(BigDecimal.valueOf(450_000)) <= 0;
                default -> insuredAmount.compareTo(BigDecimal.valueOf(375_000)) <= 0;
            };
            case NO_INFORMATION -> switch (category) {
                case LIFE, RESIDENTIAL -> insuredAmount.compareTo(BigDecimal.valueOf(200_000)) <= 0;
                case AUTO -> insuredAmount.compareTo(BigDecimal.valueOf(75_000)) <= 0;
                default -> insuredAmount.compareTo(BigDecimal.valueOf(55_000)) <= 0;
            };
        };
    }
}
