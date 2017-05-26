package com.savoirfairelinux.flashlight.service.model;

import java.util.List;
import com.liferay.portal.search.web.facet.SearchFacet;

/**
 * Contains information about a search page. Contains the search results themselves and query metadata that can be used
 * by the view.
 */
public class SearchPage {

    private List<SearchResult> searchResults;
    private int totalResults;
    private List<SearchFacet> searchFacets;

    /**
     * Creates a search page
     *
     * @param searchResults The search results contained in this page
     * @param totalSearchResults The total amount of search results (including this page)
     */
    public SearchPage(List<SearchResult> searchResults, int totalSearchResults, List<SearchFacet> searchFacets) {
        this.totalResults = totalSearchResults;
        this.searchResults = searchResults;
        this.searchFacets = searchFacets;
    }

    /**
     * @return The search results contained in this page
     */
    public List<SearchResult> getSearchResults() {
        return this.searchResults;
    }

    /**
     * @return The total amount of search results in this page, including the search results outside the current offset
     */
    public int getTotalSearchResults() {
        return this.totalResults;
    }

    public List<SearchFacet> getSearchFacets() {
        return searchFacets;
    }
}
