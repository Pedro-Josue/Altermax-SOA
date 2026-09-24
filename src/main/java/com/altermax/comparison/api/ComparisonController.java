package com.altermax.comparison.api;

import com.altermax.comparison.application.ComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/comparisons")
public class ComparisonController {
    private final ComparisonService service;

    public ComparisonController(ComparisonService service) {
        this.service = service;
    }

    @Operation(
            summary = "Cria, normaliza e registra uma comparacao",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Comparacao criada e registrada no historico"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Requisicao invalida, inclusive atributos repetidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Veiculo Ford ou concorrente nao encontrado"),
                @ApiResponse(responseCode = "422", description = "Atributo nao suportado"),
                @ApiResponse(responseCode = "502", description = "Resposta externa invalida"),
                @ApiResponse(responseCode = "503", description = "Provedor indisponivel")
            })
    @PostMapping
    public ResponseEntity<ComparisonResponse> create(
            @Valid
            @RequestBody CreateComparisonRequest request, Authentication authentication) {
        var comparison = service.compare(request, authentication.getName());
        var historyUri =
                ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/comparison-history/{id}")
                        .buildAndExpand(comparison.comparisonId())
                        .toUri();
        return ResponseEntity.created(historyUri).body(comparison);
    }
}
