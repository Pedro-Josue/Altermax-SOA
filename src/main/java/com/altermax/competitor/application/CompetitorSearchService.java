package com.altermax.competitor.application;

import com.altermax.competitor.api.CompetitorSummary;
import com.altermax.competitor.infrastructure.carsdata.CarsDataClient;
import com.altermax.competitor.infrastructure.carsdata.CarsDataException;
import com.altermax.competitor.infrastructure.carsdata.CarsDataResponseMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CompetitorSearchService {
    private final CarsDataClient client;
    private final CarsDataResponseMapper mapper;
    private final CompetitorMetadataStore metadataStore;

    public CompetitorSearchService(
            CarsDataClient client,
            CarsDataResponseMapper mapper,
            CompetitorMetadataStore metadataStore) {
        this.client = client;
        this.mapper = mapper;
        this.metadataStore = metadataStore;
    }

    public List<CompetitorSummary> search(String query) {
        try {
            List<CompetitorSummary> summaries = mapper.search(client.search(query));
            metadataStore.saveAll(summaries);
            return summaries;
        } catch (CarsDataException ex) {
            throw CompetitorErrorTranslator.translate(ex);
        }
    }
}
