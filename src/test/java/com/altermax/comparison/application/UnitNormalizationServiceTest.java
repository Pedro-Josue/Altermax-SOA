package com.altermax.comparison.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.altermax.comparison.domain.SpecificationAttribute;
import org.junit.jupiter.api.Test;

class UnitNormalizationServiceTest {
    private final UnitNormalizationService service = new UnitNormalizationService();

    @Test
    void converteKwParaHp() {
        assertThat(service.normalize(SpecificationAttribute.POWER_HP, 100, "kW").toString())
                .isEqualTo("134.1");
    }

    @Test
    void converteLbFtParaNm() {
        assertThat(service.normalize(SpecificationAttribute.TORQUE_NM, 100, "lb-ft").toString())
                .isEqualTo("135.58");
    }

    @Test
    void converteLitrosParaCc() {
        assertThat(service.normalize(SpecificationAttribute.ENGINE_DISPLACEMENT_CC, "2.0", "L"))
                .isEqualTo(2000L);
    }
}
