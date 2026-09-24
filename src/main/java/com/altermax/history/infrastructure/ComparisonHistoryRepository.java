package com.altermax.history.infrastructure;

import com.altermax.history.domain.ComparisonHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ComparisonHistoryRepository extends JpaRepository<ComparisonHistory, Long> {
    List<ComparisonHistory> findAllByRequestedByUserIdOrderByCreatedAtDesc(Long userId);

    List<ComparisonHistory> findAllByOrderByCreatedAtDesc();
}
