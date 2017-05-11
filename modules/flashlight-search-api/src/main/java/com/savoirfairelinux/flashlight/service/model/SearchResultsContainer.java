package com.savoirfairelinux.flashlight.service.model;

import java.util.List;
import java.util.Map;

import com.liferay.dynamic.data.mapping.model.DDMStructure;

/**
 * The search results container is the main model used by the view to display the relevant information about the search
 * that was performed
 */
public class SearchResultsContainer {

    private Map<DDMStructure, List<SearchResult>> searchResults;

    public SearchResultsContainer(Map<DDMStructure, List<SearchResult>> searchResults) {
        this.searchResults = searchResults;
    }

    /**
     * @return The search results, indexed by DDM structure
     */
    public Map<DDMStructure, List<SearchResult>> getSearchResults() {
        return this.searchResults;
    }

    public boolean hasSearchResults(DDMStructure structure) {
        return (this.searchResults.containsKey(structure) && !this.searchResults.get(structure).isEmpty());
    }

    public List<SearchResult> getSearchResults(DDMStructure structure) {
        return this.searchResults.get(structure);
    }

}
