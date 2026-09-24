package com.altermax.shared.config;

import com.altermax.shared.error.ApiError;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI altermaxOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Altermax API")
                                .version("1.0.0")
                                .description(
                                        "API REST para comparacao normalizada entre Ford local "
                                                + "e concorrentes Cars-Data."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(
                        new Components()
                                .schemas(ModelConverters.getInstance().read(ApiError.class))
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")));
    }

    @Bean
    OpenApiCustomizer standardizedErrors() {
        return openApi -> {
            openApi.getComponents()
                    .getSchemas()
                    .putAll(ModelConverters.getInstance().read(ApiError.class));
            openApi.getPaths()
                    .values()
                    .forEach(
                            path ->
                                    path.readOperations()
                                            .forEach(
                                                    operation -> {
                                                        boolean publicOperation =
                                                                operation.getSecurity() != null
                                                                        && operation
                                                                                .getSecurity()
                                                                                .isEmpty();
                                                        operation
                                                                .getResponses()
                                                                .forEach(
                                                                        (code, response) -> {
                                                                            if (code.startsWith("4")
                                                                                    || code
                                                                                            .startsWith(
                                                                                                    "5")) {
                                                                                response.setContent(
                                                                                        errorContent());
                                                                            }
                                                                        });
                                                        operation
                                                                .getResponses()
                                                                .putIfAbsent(
                                                                        "400",
                                                                        error(
                                                                                "Requisicao invalida"));
                                                        if (!publicOperation) {
                                                            operation
                                                                    .getResponses()
                                                                    .putIfAbsent(
                                                                            "401",
                                                                            error(
                                                                                    "JWT ausente, invalido ou expirado"));
                                                            operation
                                                                    .getResponses()
                                                                    .putIfAbsent(
                                                                            "403",
                                                                            error(
                                                                                    "Permissao insuficiente"));
                                                        }
                                                        operation
                                                                .getResponses()
                                                                .putIfAbsent(
                                                                        "500",
                                                                        error(
                                                                                "Erro interno sanitizado"));
                                                    }));
        };
    }

    private ApiResponse error(String description) {
        return new ApiResponse().description(description).content(errorContent());
    }

    private Content errorContent() {
        return new Content()
                .addMediaType(
                        "application/json",
                        new MediaType()
                                .schema(
                                        new io.swagger.v3.oas.models.media.Schema<>()
                                                .$ref("#/components/schemas/ApiError")));
    }
}
