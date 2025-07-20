package com.zagdev.insurances.domain.rules;

import com.zagdev.insurances.domain.enums.InsuranceCategory;
import com.zagdev.insurances.domain.enums.RiskClassification;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RiskRulesTest {

    static Stream<Scenario> provideRiskScenarios() {
        return Stream.of(
                new Scenario(RiskClassification.REGULAR, InsuranceCategory.LIFE, BigDecimal.valueOf(500_000), true),
                new Scenario(RiskClassification.REGULAR, InsuranceCategory.LIFE, BigDecimal.valueOf(500_001), false),
                new Scenario(RiskClassification.REGULAR, InsuranceCategory.RESIDENTIAL, BigDecimal.valueOf(500_000), true),
                new Scenario(RiskClassification.REGULAR, InsuranceCategory.RESIDENTIAL, BigDecimal.valueOf(500_001), false),
                new Scenario(RiskClassification.REGULAR, InsuranceCategory.AUTO, BigDecimal.valueOf(350_000), true),
                new Scenario(RiskClassification.REGULAR, InsuranceCategory.AUTO, BigDecimal.valueOf(350_001), false),
                new Scenario(RiskClassification.REGULAR, InsuranceCategory.OTHER, BigDecimal.valueOf(255_000), true),
                new Scenario(RiskClassification.REGULAR, InsuranceCategory.OTHER, BigDecimal.valueOf(255_001), false),

                new Scenario(RiskClassification.HIGH_RISK, InsuranceCategory.AUTO, BigDecimal.valueOf(250_000), true),
                new Scenario(RiskClassification.HIGH_RISK, InsuranceCategory.AUTO, BigDecimal.valueOf(250_001), false),
                new Scenario(RiskClassification.HIGH_RISK, InsuranceCategory.RESIDENTIAL, BigDecimal.valueOf(150_000), true),
                new Scenario(RiskClassification.HIGH_RISK, InsuranceCategory.RESIDENTIAL, BigDecimal.valueOf(150_001), false),
                new Scenario(RiskClassification.HIGH_RISK, InsuranceCategory.OTHER, BigDecimal.valueOf(125_000), true),
                new Scenario(RiskClassification.HIGH_RISK, InsuranceCategory.OTHER, BigDecimal.valueOf(125_001), false),

                new Scenario(RiskClassification.PREFERENTIAL, InsuranceCategory.LIFE, BigDecimal.valueOf(800_000), true),
                new Scenario(RiskClassification.PREFERENTIAL, InsuranceCategory.LIFE, BigDecimal.valueOf(800_001), false),
                new Scenario(RiskClassification.PREFERENTIAL, InsuranceCategory.AUTO, BigDecimal.valueOf(450_000), true),
                new Scenario(RiskClassification.PREFERENTIAL, InsuranceCategory.AUTO, BigDecimal.valueOf(450_001), false),
                new Scenario(RiskClassification.PREFERENTIAL, InsuranceCategory.RESIDENTIAL, BigDecimal.valueOf(450_000), true),
                new Scenario(RiskClassification.PREFERENTIAL, InsuranceCategory.RESIDENTIAL, BigDecimal.valueOf(450_001), false),
                new Scenario(RiskClassification.PREFERENTIAL, InsuranceCategory.OTHER, BigDecimal.valueOf(375_000), true),
                new Scenario(RiskClassification.PREFERENTIAL, InsuranceCategory.OTHER, BigDecimal.valueOf(375_001), false),

                new Scenario(RiskClassification.NO_INFORMATION, InsuranceCategory.LIFE, BigDecimal.valueOf(200_000), true),
                new Scenario(RiskClassification.NO_INFORMATION, InsuranceCategory.LIFE, BigDecimal.valueOf(200_001), false),
                new Scenario(RiskClassification.NO_INFORMATION, InsuranceCategory.RESIDENTIAL, BigDecimal.valueOf(200_000), true),
                new Scenario(RiskClassification.NO_INFORMATION, InsuranceCategory.RESIDENTIAL, BigDecimal.valueOf(200_001), false),
                new Scenario(RiskClassification.NO_INFORMATION, InsuranceCategory.AUTO, BigDecimal.valueOf(75_000), true),
                new Scenario(RiskClassification.NO_INFORMATION, InsuranceCategory.AUTO, BigDecimal.valueOf(75_001), false),
                new Scenario(RiskClassification.NO_INFORMATION, InsuranceCategory.OTHER, BigDecimal.valueOf(55_000), true),
                new Scenario(RiskClassification.NO_INFORMATION, InsuranceCategory.OTHER, BigDecimal.valueOf(55_001), false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideRiskScenarios")
    void shouldValidateRiskRulesCorrectly(Scenario scenario) {
        boolean approved = RiskRules.isApproved(
                scenario.classification,
                scenario.category,
                scenario.amount
        );
        assertEquals(scenario.expectedApproved, approved);
    }

    static class Scenario {
        RiskClassification classification;
        InsuranceCategory category;
        BigDecimal amount;
        boolean expectedApproved;

        public Scenario(RiskClassification classification, InsuranceCategory category, BigDecimal amount, boolean expectedApproved) {
            this.classification = classification;
            this.category = category;
            this.amount = amount;
            this.expectedApproved = expectedApproved;
        }
    }
}
