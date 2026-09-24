package com.altermax.comparison.application;

import com.altermax.auth.api.AuthenticatedUserProvider;
import com.altermax.comparison.api.ComparisonResponse;
import com.altermax.comparison.api.ComparisonVehicle;
import com.altermax.comparison.api.CreateComparisonRequest;
import com.altermax.comparison.domain.SpecificationAttribute;
import com.altermax.competitor.api.CompetitorCatalog;
import com.altermax.fordcatalog.api.FordVehicleCatalog;
import com.altermax.history.api.ComparisonHistoryRecorder;
import com.altermax.history.api.HistoryRecordCommand;
import com.altermax.shared.error.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ComparisonService {
    private final FordVehicleCatalog ford;
    private final CompetitorCatalog competitors;
    private final SpecificationNormalizationService normalization;
    private final ComparisonHistoryRecorder history;
    private final AuthenticatedUserProvider users;
    private final ObjectMapper mapper;

    public ComparisonService(
            FordVehicleCatalog ford,
            CompetitorCatalog competitors,
            SpecificationNormalizationService normalization,
            ComparisonHistoryRecorder history,
            AuthenticatedUserProvider users,
            ObjectMapper mapper) {
        this.ford = ford;
        this.competitors = competitors;
        this.normalization = normalization;
        this.history = history;
        this.users = users;
        this.mapper = mapper;
    }

    public ComparisonResponse compare(CreateComparisonRequest request, String username) {
        List<String> attributes = validate(request.attributes());
        var fordVehicle = ford.getById(request.fordVehicleId());
        var competitor = competitors.getDetails(request.competitorExternalId());
        Instant createdAt = Instant.now();
        var vehicles =
                List.of(
                        new ComparisonVehicle(
                                "LOCAL_FORD_CATALOG",
                                fordVehicle.brand(),
                                fordVehicle.model(),
                                fordVehicle.variant(),
                                normalization.ford(attributes, fordVehicle.specifications())),
                        new ComparisonVehicle(
                                "CARS_DATA_API",
                                competitor.brand(),
                                competitor.model(),
                                competitor.variant(),
                                normalization.competitor(attributes, competitor.specifications())));
        try {
            String resultSnapshotJson =
                    mapper.writeValueAsString(
                            Map.of(
                                    "createdAt",
                                    createdAt,
                                    "attributes",
                                    attributes,
                                    "vehicles",
                                    vehicles));
            Long comparisonId =
                    history.record(
                            new HistoryRecordCommand(
                                    createdAt,
                                    users.requiredId(username),
                                    fordVehicle.id(),
                                    competitor.externalId(),
                                    displayName(
                                            competitor.brand(),
                                            competitor.model(),
                                            competitor.variant()),
                                    attributes,
                                    resultSnapshotJson));
            return new ComparisonResponse(comparisonId, createdAt, attributes, vehicles);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Falha ao criar snapshot", ex);
        }
    }

    private List<String> validate(List<String> input) {
        var uniqueAttributes = new LinkedHashSet<String>();
        for (String rawAttribute : input) {
            String key = rawAttribute.trim().toLowerCase(Locale.ROOT);
            if (SpecificationAttribute.fromKey(key) == null) {
                throw new ApiException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "ATTRIBUTE_NOT_SUPPORTED",
                        "Atributo nao suportado: " + key);
            }
            if (!uniqueAttributes.add(key)) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_ERROR",
                        "Atributos nao podem se repetir: " + key);
            }
        }
        return List.copyOf(uniqueAttributes);
    }

    private String displayName(String... parts) {
        return String.join(
                " ",
                Arrays.stream(parts)
                        .filter(Objects::nonNull)
                        .filter(part -> !part.isBlank())
                        .toList());
    }
}
