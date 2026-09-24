package com.altermax.competitor.infrastructure.carsdata;

import static org.assertj.core.api.Assertions.assertThat;

import com.altermax.competitor.api.CompetitorSpecification;
import com.altermax.competitor.api.CompetitorSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class CarsDataResponseMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CarsDataResponseMapper mapper = new CarsDataResponseMapper();

    @Test
    void mapeiaPesquisaComOsAliasesReaisDoProvedor() throws Exception {
        var result = mapper.search(fixture("search-corolla-provider.json"));

        assertThat(result)
                .containsExactly(
                        new CompetitorSummary(
                                "35882",
                                "Toyota",
                                "Corolla",
                                "7721",
                                "Toyota Corolla 1.8 16v VVTL-i T Sport Compressor",
                                2004));
    }

    @Test
    void converteIdsNumericosParaTextoEPrefereYearFrom() throws Exception {
        var summary = mapper.search(fixture("search-corolla-provider.json")).getFirst();

        assertThat(summary.externalId()).isEqualTo("35882");
        assertThat(summary.generation()).isEqualTo("7721");
        assertThat(summary.year()).isEqualTo(2004);
    }

    @Test
    void transformaSlugsEmApresentacaoLegivel() throws Exception {
        var summary = mapper.search(fixture("search-mercedes-provider.json")).getFirst();

        assertThat(summary.brand()).isEqualTo("Mercedes Benz");
        assertThat(summary.model()).isEqualTo("C Class");
    }

    @Test
    void mantemAliasesAntigosComoFallback() throws Exception {
        var result =
                mapper.search(
                        objectMapper.readTree(
                                """
                {"items":[{
                  "externalId":"v1",
                  "make":"Honda",
                  "model":"Civic",
                  "generation":"X",
                  "trim":"Touring",
                  "model_year":2024
                }]}
                """));

        assertThat(result)
                .containsExactly(
                        new CompetitorSummary("v1", "Honda", "Civic", "X", "Touring", 2024));
    }

    @Test
    void mantemResultadoComApenasIdEUsaYearToComoUltimoFallback() throws Exception {
        var result =
                mapper.search(
                        objectMapper.readTree(
                                """
                {"data":[{"variant_id":99,"externalId":"legado","id":"antigo","year_to":2007}]}
                """));

        assertThat(result)
                .containsExactly(new CompetitorSummary("99", null, null, null, null, 2007));
    }

    @Test
    void aceitaPesquisaSemResultado() throws Exception {
        assertThat(mapper.search(objectMapper.readTree("{\"items\":[]}"))).isEmpty();
    }

    @Test
    void mapeiaTodosOsMetadadosDiretamenteDoDetalhe() throws Exception {
        var details =
                mapper.details(
                        "solicitado",
                        fixture("details-full-provider.json"),
                        fixture("specifications-provider.json"));

        assertThat(details.externalId()).isEqualTo("40001");
        assertThat(details.brand()).isEqualTo("Mercedes Benz");
        assertThat(details.model()).isEqualTo("C Class");
        assertThat(details.generation()).isEqualTo("9001");
        assertThat(details.variant()).isEqualTo("Mercedes-Benz C-Class C 300");
        assertThat(details.year()).isEqualTo(2022);
    }

    @Test
    void usaResumoComoFallbackQuandoDetalheOmiteMetadados() throws Exception {
        var fallback =
                new CompetitorSummary(
                        "103927",
                        "Toyota",
                        "Hilux",
                        "geracao-da-pesquisa",
                        "versao-da-pesquisa",
                        2021);

        var details =
                mapper.details(
                        "103927",
                        fixture("details-partial-provider.json"),
                        fixture("specifications-provider.json"),
                        fallback);

        assertThat(details.externalId()).isEqualTo("103927");
        assertThat(details.brand()).isEqualTo("Toyota");
        assertThat(details.model()).isEqualTo("Hilux");
        assertThat(details.year()).isEqualTo(2021);
        assertThat(details.generation()).isEqualTo("7794");
        assertThat(details.variant()).isEqualTo("Toyota Hilux Xtra Cabine 2.8 D-4D 4WD Invincible");
    }

    @Test
    void valoresDoDetalheTemPrecedenciaSobreFallback() throws Exception {
        var fallback =
                new CompetitorSummary(
                        "fallback",
                        "Outra Marca",
                        "Outro Modelo",
                        "outra-geracao",
                        "outra-versao",
                        1999);

        var details =
                mapper.details(
                        "solicitado",
                        fixture("details-full-provider.json"),
                        fixture("specifications-provider.json"),
                        fallback);

        assertThat(details.externalId()).isEqualTo("40001");
        assertThat(details.brand()).isEqualTo("Mercedes Benz");
        assertThat(details.model()).isEqualTo("C Class");
        assertThat(details.generation()).isEqualTo("9001");
        assertThat(details.variant()).isEqualTo("Mercedes-Benz C-Class C 300");
        assertThat(details.year()).isEqualTo(2022);
    }

    @Test
    void preservaIntegralmenteOMapeamentoDasSpecifications() throws Exception {
        var details =
                mapper.details(
                        "103927",
                        fixture("details-partial-provider.json"),
                        fixture("specifications-provider.json"));

        assertThat(details.specifications())
                .containsExactly(
                        new CompetitorSpecification("power_hp", 204, "hp"),
                        new CompetitorSpecification("top_speed_kmh", 195, "km/h"),
                        new CompetitorSpecification("torque_nm", 500, "Nm"),
                        new CompetitorSpecification("acceleration_0_100_s", 10.5, "s"),
                        new CompetitorSpecification("fuel_type", "diesel", null));
    }

    private JsonNode fixture(String name) throws IOException {
        try (InputStream input =
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/fixtures/carsdata/" + name),
                        "Fixture nao encontrada: " + name)) {
            return objectMapper.readTree(input);
        }
    }
}
