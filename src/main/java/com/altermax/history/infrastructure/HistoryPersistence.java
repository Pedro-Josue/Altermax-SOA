package com.altermax.history.infrastructure;

import com.altermax.history.domain.ComparisonHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class HistoryPersistence {
    private final ComparisonHistoryRepository repository;

    public HistoryPersistence(ComparisonHistoryRepository repository) {
        this.repository = repository;
    }

    public ComparisonHistory save(ComparisonHistory history) {
        return repository.save(history);
    }

    public Optional<ComparisonHistory> findById(Long id) {
        return repository.findById(id);
    }

    public List<ComparisonHistory> findByUser(Long id) {
        return repository.findAllByRequestedByUserIdOrderByCreatedAtDesc(id);
    }

    public List<ComparisonHistory> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
