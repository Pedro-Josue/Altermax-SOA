package com.altermax.competitor.api;

import java.util.List;

public interface CompetitorCatalog {
    List<CompetitorSummary> search(String query);

    CompetitorDetails getDetails(String externalId);
}
