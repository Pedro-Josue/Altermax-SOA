package com.altermax.competitor.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/competitors")
@Validated
public class CompetitorController {
    private final CompetitorCatalog catalog;

    public CompetitorController(CompetitorCatalog catalog) {
        this.catalog = catalog;
    }

    @Operation(summary = "Pesquisa variantes concorrentes na Cars-Data")
    @GetMapping("/search")
    public SearchResponse search(
            @RequestParam
            @NotBlank String q) {
        return new SearchResponse(catalog.search(q.trim()));
    }

    @Operation(summary = "Consulta e normaliza uma variante concorrente")
    @GetMapping("/{externalId}")
    public CompetitorDetails details(
            @PathVariable
            @NotBlank String externalId) {
        return catalog.getDetails(externalId);
    }

    public record SearchResponse(List<CompetitorSummary> items) {}
}
