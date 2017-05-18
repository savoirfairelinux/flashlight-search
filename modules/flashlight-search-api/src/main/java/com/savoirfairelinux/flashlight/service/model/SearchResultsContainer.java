package com.savoirfairelinux.flashlight.service.model;

import java.util.Map;

import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;

/**
 * The search results container is the main model used by the view to display the relevant information about the search
 * that was performed
 */
public class SearchResultsContainer {

    private Map<FlashlightSearchConfigurationTab, SearchPage> searchPages;

    /**
     * Creates the container
     * @param searchPages The search pages, indexed by tab
     */
    public SearchResultsContainer(Map<FlashlightSearchConfigurationTab, SearchPage> searchPages) {
        this.searchPages = searchPages;
    }

    /**
     * @return The search pages, indexed by DDM structure
     */
    public Map<FlashlightSearchConfigurationTab, SearchPage> getSearchPages() {
        return this.searchPages;
    }

    /**
     * @return True if there are any results in any of the pages
     */
    public boolean hasSearchResults() {
        return this.searchPages.values()
            .stream()
            .map(page -> (page.getTotalSearchResults() > 0))
            .reduce(false, (page1HasResults, page2HasResults) -> page1HasResults || page2HasResults);
    }

    /**
     * Indicates if a tab's search page has search results
     *
     * @param tab the tab in which to look for
     * @return True if there are results in the given tab's search page
     */
    public boolean hasSearchResults(FlashlightSearchConfigurationTab tab) {
        return this.searchPages.get(tab).getTotalSearchResults() > 0;
    }

    /**
     * Returns a search tab's page
     *
     * @param tab The tab from which to return the page
     * @return The search tab's page
     */
    public SearchPage getSearchPage(FlashlightSearchConfigurationTab tab) {
        return this.searchPages.get(tab);
    }

    /**
     * @return The total amount of search results in all pages.
     */
    public int getTotalSearchResults() {
        return this.searchPages.values().stream().mapToInt(SearchPage::getTotalSearchResults).sum();
    }

}
