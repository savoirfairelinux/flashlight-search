package com.savoirfairelinux.flashlight.service.configuration;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the portlet's configuration. This is a read-only view of the configuration. The returned lists and maps are
 * unmodifiable. Use Flashlight's search service to store and retreive configuration items.
 */
public class FlashlightSearchConfiguration {

    private String adtUUID;
    private boolean doSearchOnStartup;
    private String doSearchOnStartupKeywords;
    private Map<String, FlashlightSearchConfigurationTab> tabs;

    /**
     * Creates the configuration model
     *
     * @param adtUUID The ADT's UUID
     * @param doSearchOnStartup True to perform a search as soon as the search page is visited
     * @param doSearchOnStartupKeywords The keywords to send to the search if a search is triggered on startup
     * @param tabs the search groups/tabs configuration
     */
    public FlashlightSearchConfiguration(String adtUUID, boolean doSearchOnStartup, String doSearchOnStartupKeywords, List<FlashlightSearchConfigurationTab> tabs) {
        this.adtUUID = adtUUID;
        this.doSearchOnStartup = doSearchOnStartup;
        this.doSearchOnStartupKeywords = doSearchOnStartupKeywords;
        this.tabs = new LinkedHashMap<>(tabs.size());
        tabs.stream()
            .sorted(Comparator.comparing(FlashlightSearchConfigurationTab::getOrder))
            .forEach(t -> this.tabs.put(t.getId(), t));
        this.tabs = Collections.unmodifiableMap(this.tabs);
    }

    /**
     * @return The ADT's UUID
     */
    public String getAdtUUID() {
        return this.adtUUID;
    }

    /**
     * @return True to perform a search on startup
     */
    public boolean doSearchOnStartup() {
        return this.doSearchOnStartup;
    }

    /**
     * @return The keywords to send to the search if a search is triggered on startup
     */
    public String getDoSearchOnStartupKeywords() {
        return this.doSearchOnStartupKeywords;
    }

    /**
     * @return The search configuration tabs, sorted in order, indexed by ID
     */
    public Map<String, FlashlightSearchConfigurationTab> getTabs() {
        return this.tabs;
    }

}
