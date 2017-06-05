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
    private Map<String, FlashlightSearchConfigurationTab> tabs;

    /**
     * Creates the configuration model
     *
     * @param adtUUID The ADT's UUID
     * @param tabs the search groups/tabs configuration
     */
    public FlashlightSearchConfiguration(String adtUUID, List<FlashlightSearchConfigurationTab> tabs) {
        this.adtUUID = adtUUID;
        this.tabs = new LinkedHashMap<>(tabs.size());
        tabs.stream()
            .sorted(Comparator.comparing(FlashlightSearchConfigurationTab::getOrder))
            .forEach(t -> {
                this.tabs.put(t.getId(), t);
            });
        this.tabs = Collections.unmodifiableMap(this.tabs);
    }

    /**
     * @return The ADT's UUID
     */
    public String getAdtUUID() {
        return this.adtUUID;
    }

    /**
     * @return The search configuration tabs, sorted in order, indexed by ID
     */
    public Map<String, FlashlightSearchConfigurationTab> getTabs() {
        return tabs;
    }

}
