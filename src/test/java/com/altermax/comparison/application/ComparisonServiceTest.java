package com.altermax.comparison.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.altermax.auth.api.AuthenticatedUserProvider;
import com.altermax.comparison.api.CreateComparisonRequest;
import com.altermax.competitor.api.CompetitorCatalog;
import com.altermax.competitor.api.CompetitorDetails;
import com.altermax.competitor.api.CompetitorSpecification;
import com.altermax.fordcatalog.api.FordSpecificationView;
import com.altermax.fordcatalog.api.FordVehicleCatalog;
import com.altermax.fordcatalog.api.FordVehicleView;
import com.altermax.history.api.ComparisonHistoryRecorder;
import com.altermax.shared.error.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ComparisonServiceTest {
    FordVehicleCatalog ford = mock(FordVehicleCatalog.class);
    CompetitorCatalog competitor = mock(CompetitorCatalog.class);
    ComparisonHistoryRecorder history = mock(ComparisonHistoryRecorder.class);
    AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
    ComparisonService service =
            new ComparisonService(
                    ford,
                    competitor,
                    new SpecificationNormalizationService(new UnitNormalizationService()),
                    history,
                    users,
                    new ObjectMapper().findAndRegisterModules());

    @BeforeEach
    void setup() {
        when(users.requiredId("user")).thenReturn(10L);
        when(ford.getById(1L))
                .thenReturn(
                        new FordVehicleView(
                                1L,
                                "Ford",
                                "Territory",
                                "Titanium",
                                2025,
                                "BR",
                                List.of(
                                        new FordSpecificationView(
                                                "power_hp",
                                                "Potencia",
                                                "performance",
                                                "169",
                                                "hp"))));
        when(competitor.getDetails("ext-1"))
                .thenReturn(
                        new CompetitorDetails(
                                "ext-1",
                                "Toyota",
                                "Corolla",
                                null,
                                "Altis",
                                2025,
                                List.of(
                                        new CompetitorSpecification("power_hp", 103, "kW"),
                                        new CompetitorSpecification("torque_nm", null, "Nm"))));
        when(history.record(any())).thenReturn(42L);
    }

    @Test
    void comparaMultiplosAtributosNormalizaAusenciasEGeraHistorico() {
        var result =
                service.compare(
                        new CreateComparisonRequest(1L, "ext-1", List.of("power_hp", "torque_nm")),
                        "user");

        assertThat(result.comparisonId()).isEqualTo(42L);
        assertThat(result.vehicles()).hasSize(2);
        assertThat(result.vehicles().get(0).specifications().get("torque_nm").available())
                .isFalse();
        assertThat(result.vehicles().get(1).specifications().get("power_hp").value().toString())
                .isEqualTo("138.13");
        assertThat(result.vehicles().get(1).specifications().get("torque_nm").available())
                .isFalse();
        verify(history).record(any());
    }

    @Test
    void atributoInvalidoNaoConsultaNemRegistra() {
        assertThatThrownBy(
                        () ->
                                service.compare(
                                        new CreateComparisonRequest(
                                                1L, "ext-1", List.of("unknown")),
                                        "user"))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("ATTRIBUTE_NOT_SUPPORTED");
        verifyNoInteractions(ford, competitor, history);
    }

    @Test
    void falhaExternaNaoCriaHistorico() {
        when(competitor.getDetails("ext-1"))
                .thenThrow(
                        new ApiException(
                                HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_UNAVAILABLE", "x"));

        assertThatThrownBy(
                        () ->
                                service.compare(
                                        new CreateComparisonRequest(
                                                1L, "ext-1", List.of("power_hp")),
                                        "user"))
                .isInstanceOf(ApiException.class);
        verifyNoInteractions(history);
    }
}
