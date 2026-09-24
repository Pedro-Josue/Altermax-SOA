package com.altermax.comparison.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CreateComparisonRequest(
        @Schema(
                        description = "ID de um veiculo retornado por GET /api/ford-vehicles",
                        example = "1")
                @NotNull
                @Positive
                Long fordVehicleId,
        @Schema(
                        description =
                                "ID externo da variante escolhida na pesquisa de concorrentes",
                        example = "103927")
                @NotBlank
                String competitorExternalId,
        @Schema(
                        description =
                                "Lista nao vazia e sem repeticoes das chaves de atributos documentadas no README",
                        example = "[\"power_hp\", \"torque_nm\", \"transmission\", \"fuel_type\"]")
                @NotEmpty
                List<@NotBlank String> attributes) {}
