package com.savoirfairelinux.flashlight.portlet.configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Holds the portlet's configuration. This is a read-only view of the configuration. The returned lists and maps are
 * unmodifiable. Use Flashlight's search service to store and retreive configuration items.
 */
public class FlashlightConfiguration {

    private List<String> selectedFacets;
    private List<String> selectedAssetTypes;
    private Map<String, String> contentTemplates;
    private String adtUUID;

    /**
     * Creates the configuration model
     *
     * @param selectedFacets The selected facets. A read-only view of this element will be stored.
     * @param selectedAssetTypes The selected asset types. A read-only view of this element will be stored.
     * @param contentTemplates The selected content templates. A read-only view of this element will be stored.
     * @param adtUUID The ADT's UUID
     */
    public FlashlightConfiguration(List<String> selectedFacets, List<String> selectedAssetTypes, Map<String, String> contentTemplates, String adtUUID) {
        this.selectedFacets = Collections.unmodifiableList(selectedFacets);
        this.selectedAssetTypes = Collections.unmodifiableList(selectedAssetTypes);
        this.contentTemplates = Collections.unmodifiableMap(contentTemplates);
        this.adtUUID = adtUUID;
    }

    /**
     * @return A read-only view of the selected facet names
     */
    public List<String> getSelectedFacets() {
        return this.selectedFacets;
    }

    /**
     * @return A read-only view of the selected asset types
     */
    public List<String> getSelectedAssetTypes() {
        return this.selectedAssetTypes;
    }

    /**
     * @return A read-only view of the selected content templates
     */
    public Map<String, String> getContentTemplates() {
        return this.contentTemplates;
    }

    /**
     * @return The ADT's UUID
     */
    public String getAdtUUID() {
        return this.adtUUID;
    }

}
