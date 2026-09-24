package com.altermax.fordcatalog.api;

import com.altermax.fordcatalog.application.FordVehicleManagementService;
import com.altermax.fordcatalog.application.FordVehicleQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/ford-vehicles")
public class FordVehicleController {
    private final FordVehicleQueryService queries;
    private final FordVehicleManagementService management;

    public FordVehicleController(
            FordVehicleQueryService queries, FordVehicleManagementService management) {
        this.queries = queries;
        this.management = management;
    }

    @Operation(summary = "Lista o catalogo Ford local")
    @GetMapping
    public List<FordVehicleView> list() {
        return queries.list();
    }

    @Operation(
            summary = "Detalha um Ford e suas especificacoes",
            responses = @ApiResponse(responseCode = "404", description = "Ford inexistente"))
    @GetMapping("/{id}")
    public FordVehicleView get(@PathVariable Long id) {
        return queries.getById(id);
    }

    @Operation(
            summary = "Cadastra Ford (ADMIN)",
            responses = @ApiResponse(responseCode = "201", description = "Criado"))
    @PostMapping
    public ResponseEntity<FordVehicleView> create(
            @Valid
            @RequestBody FordVehicleRequest request) {
        var result = management.create(request);
        var uri =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(result.id())
                        .toUri();
        return ResponseEntity.created(uri).body(result);
    }

    @Operation(summary = "Substitui Ford e especificacoes (ADMIN)")
    @PutMapping("/{id}")
    public FordVehicleView update(
            @PathVariable Long id,
            @Valid
            @RequestBody FordVehicleRequest request) {
        return management.update(id, request);
    }

    @Operation(summary = "Exclui Ford (ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        management.delete(id);
    }
}
