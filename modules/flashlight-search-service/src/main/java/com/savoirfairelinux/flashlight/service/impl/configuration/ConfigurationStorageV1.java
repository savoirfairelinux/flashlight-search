package com.savoirfairelinux.flashlight.service.impl.configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import com.liferay.portal.kernel.util.StringPool;
import com.savoirfairelinux.flashlight.portlet.configuration.FlashlightConfiguration;

/**
 * First version of the configuration storage mechanism
 */
public class ConfigurationStorageV1 implements ConfigurationStorage {

    private static final String CONF_KEY_ENABLED_FACETS = "enabled-facets";
    private static final String CONF_KEY_ENABLED_ASSET_TYPES = "enabled-asset-types";
    private static final String CONF_KEY_ADT_GROUP_UUID = "adt-group-uuid";
    private static final String CONF_KEY_ADT_UUID = "adt-uuid";

    @Override
    public FlashlightConfiguration readConfiguration(PortletPreferences preferences) {
        List<String> enabledFacets = Arrays.asList(preferences.getValues(CONF_KEY_ENABLED_FACETS, new String[0]));
        List<String> enabledAssetTypes = Arrays.asList(preferences.getValues(CONF_KEY_ENABLED_ASSET_TYPES, new String[0]));
        FlashlightConfiguration config = new FlashlightConfiguration(
            enabledFacets,
            enabledAssetTypes,
            null,
            preferences.getValue(CONF_KEY_ADT_GROUP_UUID, StringPool.BLANK),
            preferences.getValue(CONF_KEY_ADT_UUID, StringPool.BLANK)
        );

        return config;
    }

    @Override
    public void writeConfiguration(FlashlightConfiguration configuration, PortletPreferences preferences) throws IOException, ValidatorException, ReadOnlyException {
        List<String> enabledFacets = configuration.getSelectedFacets();
        List<String> selectedAssetTypes = configuration.getSelectedAssetTypes();

        preferences.setValues(CONF_KEY_ENABLED_FACETS, enabledFacets.toArray(new String[enabledFacets.size()]));
        preferences.setValues(CONF_KEY_ENABLED_ASSET_TYPES, selectedAssetTypes.toArray(new String[selectedAssetTypes.size()]));
        preferences.setValue(CONF_KEY_ADT_GROUP_UUID, configuration.getAdtGroupUUID());
        preferences.setValue(CONF_KEY_ADT_UUID, configuration.getAdtUUID());
        preferences.store();
    }

    @Override
    public void migrateConfiguration(PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {}

}
