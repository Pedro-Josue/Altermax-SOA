package com.altermax.competitor.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.altermax.competitor.api.CompetitorSummary;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompetitorMetadataStoreTest {
    @Test
    void limitaOTamanhoERemoveOMetadadoMenosRecentementeUsado() {
        var store = new CompetitorMetadataStore(2);
        var first = summary("1");
        var second = summary("2");
        var third = summary("3");
        store.saveAll(List.of(first, second));
        store.findByExternalId("1");

        store.saveAll(List.of(third));

        assertThat(store.findByExternalId("1")).contains(first);
        assertThat(store.findByExternalId("2")).isEmpty();
        assertThat(store.findByExternalId("3")).contains(third);
    }

    private CompetitorSummary summary(String id) {
        return new CompetitorSummary(id, "Marca", "Modelo", "Geracao", "Versao", 2024);
    }
}
