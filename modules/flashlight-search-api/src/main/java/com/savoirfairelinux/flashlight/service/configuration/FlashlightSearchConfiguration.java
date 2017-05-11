package com.savoirfairelinux.flashlight.service.configuration;

import java.util.Collections;
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
     * @param selectedAssetTypes The selected asset types. A read-only view of this element will be stored.
     * @param contentTemplates The selected content templates. A read-only view of this element will be stored.
     * @param adtUUID The ADT's UUID
     */
    public FlashlightSearchConfiguration(String adtUUID, List<FlashlightSearchConfigurationTab> tabs) {
        this.adtUUID = adtUUID;
        this.tabs = new LinkedHashMap<>(tabs.size());
        tabs.stream()
            .sorted(FlashlightSearchConfiguration::sortByOrder)
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

    /**
     * Sorts the configuration tabs by their order
     *
     * @param a The first tab to compare
     * @param b The second tab to compare
     * @return 1 if a > b, -1 if b < a, 0 if a == b
     */
    private static int sortByOrder(FlashlightSearchConfigurationTab a, FlashlightSearchConfigurationTab b) {
        int aOrder = a.getOrder();
        int bOrder = b.getOrder();
        int val;
        if(aOrder > bOrder) {
            val = 1;
        } else if(aOrder < bOrder) {
            val = -1;
        } else {
            val = 0;
        }
        return val;
    }

}
