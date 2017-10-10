// Copyright 2017 Savoir-faire Linux
// This file is part of Flashlight Search.

// Flashlight Search is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Flashlight Search is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Flashlight Search.  If not, see <http://www.gnu.org/licenses/>.

package com.savoirfairelinux.flashlight.service.model;

import java.util.List;

/**
 * Contains information about a search page. Contains the search results themselves and query metadata that can be used
 * by the view.
 */
public class SearchPage {

    private List<SearchResult> searchResults;
    private int totalResults;
    private List<SearchResultFacet> searchFacets;

    /**
     * Creates a search page
     *
     * @param searchResults The search results contained in this page
     * @param totalSearchResults The total amount of search results (including this page)
     * @param searchFacets The facets processed from the search results
     */
    public SearchPage(List<SearchResult> searchResults, int totalSearchResults, List<SearchResultFacet> searchFacets) {
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

    /**
     * @return the processed facets contained in the result
     */
    public List<SearchResultFacet> getSearchFacets() {
        return searchFacets;
    }
}
