package com.altermax.competitor.application;

import com.altermax.competitor.api.CompetitorCatalog;
import com.altermax.competitor.api.CompetitorDetails;
import com.altermax.competitor.api.CompetitorSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CompetitorCatalogFacade implements CompetitorCatalog {
    private final CompetitorSearchService searchService;
    private final CompetitorSpecificationService specificationService;

    public CompetitorCatalogFacade(
            CompetitorSearchService searchService,
            CompetitorSpecificationService specificationService) {
        this.searchService = searchService;
        this.specificationService = specificationService;
    }

    @Override
    public List<CompetitorSummary> search(String query) {
        return searchService.search(query);
    }

    @Override
    public CompetitorDetails getDetails(String externalId) {
        return specificationService.getDetails(externalId);
    }
}
