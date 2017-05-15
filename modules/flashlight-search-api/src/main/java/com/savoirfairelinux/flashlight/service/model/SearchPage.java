package com.savoirfairelinux.flashlight.service.model;

import java.util.List;

/**
 * Contains information about a search page. Contains the search results themselves and query metadata that can be used
 * by the view.
 */
public class SearchPage {

    private List<SearchResult> searchResults;
    private int totalResults;

    /**
     * Creates a search page
     *
     * @param searchResults The search results contained in this page
     * @param totalSearchResults The total amount of search results (including this page)
     */
    public SearchPage(List<SearchResult> searchResults, int totalSearchResults) {
        this.totalResults = totalSearchResults;
        this.searchResults = searchResults;
    }

    /**
     * @return The search results contained in this page
     */
    public List<SearchResult> getSearchResults() {
        return this.searchResults;
    }

    /**
     * @return The total amount of search results (including this page)
     */
    public int getTotalSearchResults() {
        return this.totalResults;
    }

}
