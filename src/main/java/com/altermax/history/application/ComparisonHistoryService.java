package com.altermax.history.application;

import com.altermax.history.api.ComparisonHistoryRecorder;
import com.altermax.history.api.ComparisonHistoryView;
import com.altermax.history.api.HistoryRecordCommand;
import com.altermax.history.domain.ComparisonHistory;
import com.altermax.history.infrastructure.HistoryPersistence;
import com.altermax.shared.error.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComparisonHistoryService implements ComparisonHistoryRecorder {
    private final HistoryPersistence persistence;
    private final ObjectMapper mapper;

    public ComparisonHistoryService(HistoryPersistence persistence, ObjectMapper mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Long record(HistoryRecordCommand command) {
        try {
            return persistence
                    .save(
                            new ComparisonHistory(
                                    command.createdAt(),
                                    command.requestedByUserId(),
                                    command.fordVehicleId(),
                                    command.competitorExternalId(),
                                    command.competitorDisplayName(),
                                    mapper.writeValueAsString(command.requestedAttributes()),
                                    command.resultSnapshotJson()))
                    .getId();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Falha ao serializar historico", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<ComparisonHistoryView> list(Long userId, boolean admin) {
        return (admin ? persistence.findAll() : persistence.findByUser(userId))
                .stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public ComparisonHistoryView get(Long id, Long userId, boolean admin) {
        var history = persistence.findById(id).orElseThrow(ComparisonHistoryService::notFound);
        if (!admin && !history.getRequestedByUserId().equals(userId)) {
            throw notFound();
        }
        return view(history);
    }

    private ComparisonHistoryView view(ComparisonHistory history) {
        try {
            return new ComparisonHistoryView(
                    history.getId(),
                    history.getCreatedAt(),
                    history.getFordVehicleId(),
                    history.getCompetitorExternalId(),
                    history.getCompetitorDisplayName(),
                    mapper.readValue(
                            history.getRequestedAttributesJson(), new TypeReference<>() {}),
                    mapper.readTree(history.getResultSnapshotJson()));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Snapshot de historico invalido", ex);
        }
    }

    private static ApiException notFound() {
        return new ApiException(
                HttpStatus.NOT_FOUND, "COMPARISON_NOT_FOUND", "Comparacao nao encontrada.");
    }
}
