package com.altermax.competitor.application;

import com.altermax.competitor.api.CompetitorSummary;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CompetitorMetadataStore {
    private static final int DEFAULT_MAX_ENTRIES = 1_000;

    private final int maxEntries;
    private final Map<String, CompetitorSummary> summaries = new LinkedHashMap<>(16, 0.75f, true);

    public CompetitorMetadataStore() {
        this(DEFAULT_MAX_ENTRIES);
    }

    CompetitorMetadataStore(int maxEntries) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("maxEntries deve ser positivo.");
        }
        this.maxEntries = maxEntries;
    }

    public synchronized void saveAll(List<CompetitorSummary> summaries) {
        if (summaries == null) {
            return;
        }
        for (CompetitorSummary summary : summaries) {
            if (summary != null
                    && summary.externalId() != null
                    && !summary.externalId().isBlank()) {
                this.summaries.put(summary.externalId(), summary);
            }
        }
        trimToLimit();
    }

    public synchronized Optional<CompetitorSummary> findByExternalId(String externalId) {
        if (externalId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(summaries.get(externalId));
    }

    private void trimToLimit() {
        Iterator<String> iterator = summaries.keySet().iterator();
        while (summaries.size() > maxEntries && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
