package com.altermax.comparison.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.altermax.competitor.api.CompetitorSpecification;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpecificationNormalizationServiceTest {
    @Test
    void preservaSchemaEMarcaAusencia() {
        var service = new SpecificationNormalizationService(new UnitNormalizationService());
        var result =
                service.competitor(
                        List.of("power_hp", "torque_nm"),
                        List.of(new CompetitorSpecification("power_hp", 100, "kW")));
        assertThat(result).containsOnlyKeys("power_hp", "torque_nm");
        assertThat(result.get("power_hp").available()).isTrue();
        assertThat(result.get("power_hp").unit()).isEqualTo("hp");
        assertThat(result.get("torque_nm").available()).isFalse();
        assertThat(result.get("torque_nm").value()).isNull();
    }
}
