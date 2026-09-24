package com.altermax.competitor.application;

import com.altermax.competitor.api.CompetitorDetails;
import com.altermax.competitor.infrastructure.carsdata.CarsDataClient;
import com.altermax.competitor.infrastructure.carsdata.CarsDataException;
import com.altermax.competitor.infrastructure.carsdata.CarsDataResponseMapper;
import org.springframework.stereotype.Service;

@Service
public class CompetitorSpecificationService {
    private final CarsDataClient client;
    private final CarsDataResponseMapper mapper;
    private final CompetitorMetadataStore metadataStore;

    public CompetitorSpecificationService(
            CarsDataClient client,
            CarsDataResponseMapper mapper,
            CompetitorMetadataStore metadataStore) {
        this.client = client;
        this.mapper = mapper;
        this.metadataStore = metadataStore;
    }

    public CompetitorDetails getDetails(String externalId) {
        try {
            return mapper.details(
                    externalId,
                    client.variant(externalId),
                    client.specifications(externalId),
                    metadataStore.findByExternalId(externalId).orElse(null));
        } catch (CarsDataException ex) {
            throw CompetitorErrorTranslator.translate(ex);
        }
    }
}
