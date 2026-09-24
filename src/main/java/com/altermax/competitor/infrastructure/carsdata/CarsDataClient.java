package com.altermax.competitor.infrastructure.carsdata;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class CarsDataClient {
    private final RestClient restClient;
    private final CarsDataProperties properties;

    public CarsDataClient(RestClient carsDataRestClient, CarsDataProperties properties) {
        this.restClient = carsDataRestClient;
        this.properties = properties;
    }

    public JsonNode search(String query) {
        return get("/search?q={query}", query);
    }

    public JsonNode variant(String id) {
        return get("/variants/{id}", id);
    }

    public JsonNode specifications(String id) {
        return get("/variants/{id}/specs?locale=en", id);
    }

    private JsonNode get(String uri, Object variable) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new CarsDataException(
                    CarsDataException.Kind.UNAVAILABLE, "CARSDATA_API_KEY nao configurada.");
        }
        try {
            return restClient
                    .get()
                    .uri(uri, variable)
                    .header("X-Api-Key", properties.apiKey())
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new CarsDataException(
                                        CarsDataException.Kind.NOT_FOUND,
                                        "Recurso externo nao encontrado.");
                            })
                    .onStatus(
                            status -> status.value() == 429,
                            (request, response) -> {
                                throw new CarsDataException(
                                        CarsDataException.Kind.RATE_LIMIT,
                                        "Limite do provedor atingido.");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new CarsDataException(
                                        CarsDataException.Kind.UNAVAILABLE,
                                        "Provedor externo indisponivel.");
                            })
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new CarsDataException(
                                        CarsDataException.Kind.INVALID_RESPONSE,
                                        "Falha ao consultar provedor externo.");
                            })
                    .body(JsonNode.class);
        } catch (ResourceAccessException ex) {
            throw new CarsDataException(
                    CarsDataException.Kind.TIMEOUT, "Tempo limite do provedor excedido.");
        } catch (CarsDataException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CarsDataException(
                    CarsDataException.Kind.UNAVAILABLE,
                    "Nao foi possivel consultar o provedor externo.");
        }
    }
}
