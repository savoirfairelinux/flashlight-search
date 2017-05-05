package com.savoirfairelinux.flashlight.portlet.configuration;

import java.util.List;
import java.util.Map;

/**
 * Holds the portlet's configuration
 */
public class FlashlightConfiguration {

    private List<String> selectedFacets;
    private List<String> selectedAssetTypes;
    private Map<String, String> contentTemplates;
    private String adtGroupUUID;
    private String adtUUID;

    public FlashlightConfiguration(List<String> selectedFacets, List<String> selectedAssetTypes, Map<String, String> contentTemplates, String adtGroupUUID, String adtUUID) {
        this.selectedFacets = selectedFacets;
        this.selectedAssetTypes = selectedAssetTypes;
        this.contentTemplates = contentTemplates;
        this.adtGroupUUID = adtGroupUUID;
        this.adtUUID = adtUUID;
    }

    public List<String> getSelectedFacets() {
        return this.selectedFacets;
    }

    public List<String> getSelectedAssetTypes() {
        return this.selectedAssetTypes;
    }

    public Map<String, String> getContentTemplates() {
        return this.contentTemplates;
    }

    public String getAdtGroupUUID() {
        return this.adtGroupUUID;
    }

    public String getAdtUUID() {
        return this.adtUUID;
    }

}
