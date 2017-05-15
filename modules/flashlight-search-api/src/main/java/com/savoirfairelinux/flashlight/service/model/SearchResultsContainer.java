package com.savoirfairelinux.flashlight.service.model;

import java.util.List;
import java.util.Map;

import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;

/**
 * The search results container is the main model used by the view to display the relevant information about the search
 * that was performed
 */
public class SearchResultsContainer {

    private Map<FlashlightSearchConfigurationTab, List<SearchResult>> searchResults;

    public SearchResultsContainer(Map<FlashlightSearchConfigurationTab, List<SearchResult>> searchResults) {
        this.searchResults = searchResults;
    }

    /**
     * @return The search results, indexed by DDM structure
     */
    public Map<FlashlightSearchConfigurationTab, List<SearchResult>> getSearchResults() {
        return this.searchResults;
    }

    public boolean hasSearchResults(FlashlightSearchConfigurationTab tab) {
        return !this.searchResults.get(tab).isEmpty();
    }

    public List<SearchResult> getSearchResults(FlashlightSearchConfigurationTab tab) {
        return this.searchResults.get(tab);
    }

}
