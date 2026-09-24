package com.altermax.competitor.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.altermax.competitor.api.CompetitorSummary;
import com.altermax.competitor.infrastructure.carsdata.CarsDataClient;
import com.altermax.competitor.infrastructure.carsdata.CarsDataException;
import com.altermax.competitor.infrastructure.carsdata.CarsDataResponseMapper;
import com.altermax.shared.error.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class CompetitorServicesTest {
    CarsDataClient client = mock(CarsDataClient.class);
    CarsDataResponseMapper mapper = new CarsDataResponseMapper();
    CompetitorMetadataStore metadataStore = new CompetitorMetadataStore();
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void pesquisaValidaSalvaOsMetadadosReaisDoProvedor() throws Exception {
        when(client.search("Corolla")).thenReturn(fixture("search-corolla-provider.json"));

        var result = new CompetitorSearchService(client, mapper, metadataStore).search("Corolla");

        assertThat(result).hasSize(1);
        assertThat(metadataStore.findByExternalId("35882")).contains(result.getFirst());
    }

    @Test
    void fluxoPesquisaEscolhaEDetalhePreservaTodosOsMetadados() throws Exception {
        when(client.search("Hilux")).thenReturn(fixture("search-hilux-provider.json"));
        when(client.variant("103927")).thenReturn(fixture("details-partial-provider.json"));
        when(client.specifications("103927")).thenReturn(fixture("specifications-provider.json"));
        var catalog =
                new CompetitorCatalogFacade(
                        new CompetitorSearchService(client, mapper, metadataStore),
                        new CompetitorSpecificationService(client, mapper, metadataStore));

        CompetitorSummary selected = catalog.search("Hilux").getFirst();
        var details = catalog.getDetails(selected.externalId());

        assertThat(details.externalId()).isEqualTo("103927");
        assertThat(details.brand()).isEqualTo("Toyota");
        assertThat(details.model()).isEqualTo("Hilux");
        assertThat(details.generation()).isEqualTo("7794");
        assertThat(details.variant()).isEqualTo("Toyota Hilux Xtra Cabine 2.8 D-4D 4WD Invincible");
        assertThat(details.year()).isEqualTo(2021);
        assertThat(details.specifications()).hasSize(5);
    }

    @Test
    void idAusenteNoStoreNaoLancaErroIndevido() throws Exception {
        when(client.variant("sem-cache"))
                .thenReturn(
                        objectMapper.readTree(
                                """
                {"data":{"generation_id":7794,"display_name":"Toyota Hilux Invincible"}}
                """));
        when(client.specifications("sem-cache"))
                .thenReturn(fixture("specifications-provider.json"));

        var details =
                new CompetitorSpecificationService(client, mapper, metadataStore)
                        .getDetails("sem-cache");

        assertThat(details.externalId()).isEqualTo("sem-cache");
        assertThat(details.brand()).isNull();
        assertThat(details.model()).isNull();
        assertThat(details.year()).isNull();
        assertThat(details.generation()).isEqualTo("7794");
    }

    @Test
    void traduzRateLimit() {
        when(client.search(any()))
                .thenThrow(new CarsDataException(CarsDataException.Kind.RATE_LIMIT, "x"));
        assertCode(() -> searchService().search("x"), "EXTERNAL_API_RATE_LIMIT", 429);
    }

    @Test
    void traduzTimeout() {
        when(client.search(any()))
                .thenThrow(new CarsDataException(CarsDataException.Kind.TIMEOUT, "x"));
        assertCode(() -> searchService().search("x"), "EXTERNAL_API_TIMEOUT", 503);
    }

    @Test
    void traduzIndisponibilidade() {
        when(client.search(any()))
                .thenThrow(new CarsDataException(CarsDataException.Kind.UNAVAILABLE, "x"));
        assertCode(() -> searchService().search("x"), "EXTERNAL_API_UNAVAILABLE", 503);
    }

    @Test
    void traduzRespostaInvalidaParaBadGateway() {
        when(client.search(any()))
                .thenThrow(new CarsDataException(CarsDataException.Kind.INVALID_RESPONSE, "x"));
        assertCode(() -> searchService().search("x"), "EXTERNAL_API_INVALID_RESPONSE", 502);
    }

    @Test
    void traduzVarianteInexistente() {
        when(client.variant("404"))
                .thenThrow(new CarsDataException(CarsDataException.Kind.NOT_FOUND, "x"));
        assertCode(
                () ->
                        new CompetitorSpecificationService(client, mapper, metadataStore)
                                .getDetails("404"),
                "COMPETITOR_NOT_FOUND",
                404);
    }

    private CompetitorSearchService searchService() {
        return new CompetitorSearchService(client, mapper, metadataStore);
    }

    private JsonNode fixture(String name) throws IOException {
        try (InputStream input =
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/fixtures/carsdata/" + name),
                        "Fixture nao encontrada: " + name)) {
            return objectMapper.readTree(input);
        }
    }

    private void assertCode(ThrowingCallable call, String code, int status) {
        assertThatThrownBy(call)
                .isInstanceOf(ApiException.class)
                .satisfies(
                        throwable -> {
                            var apiException = (ApiException) throwable;
                            assertThat(apiException.code()).isEqualTo(code);
                            assertThat(apiException.status().value()).isEqualTo(status);
                        });
    }
}
