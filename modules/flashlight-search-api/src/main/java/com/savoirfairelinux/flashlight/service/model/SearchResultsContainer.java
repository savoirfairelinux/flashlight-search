package com.savoirfairelinux.flashlight.service.model;

import java.util.HashMap;
import java.util.Map;

import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;

/**
 * The search results container is the main model used by the view to display the relevant information about the search
 * that was performed
 */
public class SearchResultsContainer {

    private Map<FlashlightSearchConfigurationTab, SearchPage> searchPages;
    private Map<String, FlashlightSearchConfigurationTab> tabIndex;

    /**
     * Creates the container
     * @param searchPages The search pages, indexed by tab
     */
    public SearchResultsContainer(Map<FlashlightSearchConfigurationTab, SearchPage> searchPages) {
        this.searchPages = searchPages;
        this.tabIndex = new HashMap<>(searchPages.size());
        for(FlashlightSearchConfigurationTab tab : searchPages.keySet()) {
            this.tabIndex.put(tab.getId(), tab);
        }
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
     * @param tabId The tab's ID
     * @return True if there are results in the given tab's search page
     */
    public boolean hasSearchResults(String tabId) {
        boolean hasResults;

        if(this.tabIndex.containsKey(tabId)) {
            hasResults = this.hasSearchResults(this.tabIndex.get(tabId));
        } else {
            hasResults = false;
        }

        return hasResults;
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
     * @param tabId The tab from which to return the page
     * @return The search tab's page or null if none found
     */
    public SearchPage getSearchPage(String tabId) {
        SearchPage page;

        if(this.tabIndex.containsKey(tabId)) {
            page = this.getSearchPage(this.tabIndex.get(tabId));
        } else {
            page = null;
        }

        return page;
    }

    /**
     * Returns a search tab's page
     *
     * @param tab The tab from which to return the page
     * @return The search tab's page or null if none found
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
