package com.altermax.competitor.application;

import com.altermax.competitor.infrastructure.carsdata.CarsDataException;
import com.altermax.shared.error.ApiException;
import org.springframework.http.HttpStatus;

final class CompetitorErrorTranslator {
    private CompetitorErrorTranslator() {
    }

    static ApiException translate(CarsDataException failure) {
        return switch (failure.kind()) {
            case NOT_FOUND ->
                    new ApiException(
                            HttpStatus.NOT_FOUND,
                            "COMPETITOR_NOT_FOUND",
                            "Concorrente ou variante nao encontrado.");
            case RATE_LIMIT ->
                    new ApiException(
                            HttpStatus.TOO_MANY_REQUESTS,
                            "EXTERNAL_API_RATE_LIMIT",
                            "Limite de requisicoes do provedor externo atingido.");
            case TIMEOUT ->
                    new ApiException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "EXTERNAL_API_TIMEOUT",
                            "O provedor externo excedeu o tempo limite.");
            case UNAVAILABLE ->
                    new ApiException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "EXTERNAL_API_UNAVAILABLE",
                            "O provedor externo esta indisponivel.");
            case INVALID_RESPONSE ->
                    new ApiException(
                            HttpStatus.BAD_GATEWAY,
                            "EXTERNAL_API_INVALID_RESPONSE",
                            "O provedor externo retornou uma resposta invalida.");
        };
    }
}
