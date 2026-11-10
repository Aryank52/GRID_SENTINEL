package com.gridsentinel.risk;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.RiskBand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class RiskScoreTest {

    @Test
    void riskScoreCreation() {
        CommonKernel.RiskScoreId id = CommonKernel.RiskScoreId.generate();
        CommonKernel.AssetId assetId = CommonKernel.AssetId.generate();

        // FactorBreakdown is required - create a minimal one
        com.gridsentinel.risk.FactorBreakdown factorBreakdown = new com.gridsentinel.risk.FactorBreakdown();

        RiskScore score = RiskScore.builder()
                .id(id)
                .assetId(assetId)
                .score(0.75)
                .modelName("Thermal-Imaging-ML-v3")
                .modelVersion("1.0")
                .factorBreakdown(factorBreakdown)
                .build();

        assertThat(score.getId()).isEqualTo(id);
        assertThat(score.getAssetId()).isEqualTo(assetId);
        assertThat(score.getScore()).isEqualTo(0.75);
        assertThat(score.getBand()).isEqualTo(RiskBand.HIGH);
        assertThat(score.getModelName()).isEqualTo("Thermal-Imaging-ML-v3");
        assertThat(score.getModelVersion()).isEqualTo("1.0");
        assertThat(score.getFactorBreakdown()).isNotNull();
        assertThat(score.getComputedAt()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1, -1.0, 2.0})
    void riskScoreOutOfRangeThrows(double score) {
        com.gridsentinel.risk.FactorBreakdown factorBreakdown = new com.gridsentinel.risk.FactorBreakdown();

        assertThatThrownBy(() -> RiskScore.builder()
                .id(CommonKernel.RiskScoreId.generate())
                .assetId(CommonKernel.AssetId.generate())
                .score(score)
                .modelName("Test")
                .modelVersion("1.0")
                .factorBreakdown(factorBreakdown)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void riskScoreNullAssetIdThrows() {
        com.gridsentinel.risk.FactorBreakdown factorBreakdown = new com.gridsentinel.risk.FactorBreakdown();

        assertThatThrownBy(() -> RiskScore.builder()
                .id(CommonKernel.RiskScoreId.generate())
                .assetId(null)
                .score(0.5)
                .modelName("Test")
                .modelVersion("1.0")
                .factorBreakdown(factorBreakdown)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void riskScoreNullModelNameThrows() {
        com.gridsentinel.risk.FactorBreakdown factorBreakdown = new com.gridsentinel.risk.FactorBreakdown();

        assertThatThrownBy(() -> RiskScore.builder()
                .id(CommonKernel.RiskScoreId.generate())
                .assetId(CommonKernel.AssetId.generate())
                .score(0.5)
                .modelName(null)
                .modelVersion("1.0")
                .factorBreakdown(factorBreakdown)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void riskScoreBandConsistencyValidation() {
        // Band is computed from score in builder, so we can't set inconsistent band
        // The test verifies that band is correctly derived from score
        com.gridsentinel.risk.FactorBreakdown factorBreakdown = new com.gridsentinel.risk.FactorBreakdown();

        RiskScore score = RiskScore.builder()
                .id(CommonKernel.RiskScoreId.generate())
                .assetId(CommonKernel.AssetId.generate())
                .score(0.75) // This should give HIGH band
                .modelName("Test")
                .modelVersion("1.0")
                .factorBreakdown(factorBreakdown)
                .build();

        assertThat(score.getBand()).isEqualTo(RiskBand.fromScore(score.getScore()));
    }

    @Test
    void riskScoreEquality() {
        CommonKernel.RiskScoreId id = CommonKernel.RiskScoreId.generate();
        CommonKernel.AssetId assetId = CommonKernel.AssetId.generate();
        Instant computedAt = Instant.now();
        com.gridsentinel.risk.FactorBreakdown factorBreakdown = new com.gridsentinel.risk.FactorBreakdown();

        RiskScore score1 = RiskScore.builder()
                .id(id)
                .assetId(assetId)
                .score(0.75)
                .modelName("Test")
                .modelVersion("1.0")
                .factorBreakdown(factorBreakdown)
                .computedAt(computedAt)
                .build();

        RiskScore score2 = RiskScore.builder()
                .id(id)
                .assetId(assetId)
                .score(0.75)
                .modelName("Test")
                .modelVersion("1.0")
                .factorBreakdown(factorBreakdown)
                .computedAt(computedAt)
                .build();

        assertThat(score1).isEqualTo(score2);
        assertThat(score1.hashCode()).isEqualTo(score2.hashCode());
    }
}