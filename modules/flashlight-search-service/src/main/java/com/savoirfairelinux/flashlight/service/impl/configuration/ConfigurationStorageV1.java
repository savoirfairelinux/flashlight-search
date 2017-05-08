package com.savoirfairelinux.flashlight.service.impl.configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import com.liferay.portal.kernel.util.StringPool;
import com.savoirfairelinux.flashlight.portlet.configuration.FlashlightConfiguration;

/**
 * First version of the configuration storage mechanism
 */
public class ConfigurationStorageV1 implements ConfigurationStorage {

    private static final String CONF_KEY_SELECTED_FACETS = "selected-facets";
    private static final String CONF_KEY_SELECTED_ASSET_TYPES = "selected-asset-types";
    private static final String CONF_KEY_ADT_UUID = "adt-uuid";
    private static final String CONF_KEY_FORMAT_DDM = "ddm-%s";
    private static final Pattern CONF_KEY_PATTERN_DDM = Pattern.compile("^ddm-([a-f0-9]{8}-([a-f0-9]{4}-){3}[a-f0-9]{12})$");
    private static final int CONF_KEY_PATTERN_DDM_GROUP_UUID = 1;

    @Override
    public FlashlightConfiguration readConfiguration(PortletPreferences preferences) {
        // Read singular values
        List<String> selectedFacets = Arrays.asList(preferences.getValues(CONF_KEY_SELECTED_FACETS, new String[0]));
        List<String> selectedAssetTypes = Arrays.asList(preferences.getValues(CONF_KEY_SELECTED_ASSET_TYPES, new String[0]));

        // Read DDM templates configuration
        Enumeration<String> prefKeys = preferences.getNames();
        HashMap<String, String> contentTemplates = new HashMap<>();
        while(prefKeys.hasMoreElements()) {
            String key = prefKeys.nextElement();
            Matcher keyMatcher = CONF_KEY_PATTERN_DDM.matcher(key);
            if(keyMatcher.matches()) {
                contentTemplates.put(keyMatcher.group(CONF_KEY_PATTERN_DDM_GROUP_UUID), preferences.getValue(key, StringPool.BLANK));
            }
        }

        FlashlightConfiguration config = new FlashlightConfiguration(
            selectedFacets,
            selectedAssetTypes,
            contentTemplates,
            preferences.getValue(CONF_KEY_ADT_UUID, StringPool.BLANK)
        );

        return config;
    }

    @Override
    public void writeConfiguration(FlashlightConfiguration configuration, PortletPreferences preferences) throws IOException, ValidatorException, ReadOnlyException {
        List<String> enabledFacets = configuration.getSelectedFacets();
        List<String> selectedAssetTypes = configuration.getSelectedAssetTypes();

        // Write singular values
        preferences.setValues(CONF_KEY_SELECTED_FACETS, enabledFacets.toArray(new String[enabledFacets.size()]));
        preferences.setValues(CONF_KEY_SELECTED_ASSET_TYPES, selectedAssetTypes.toArray(new String[selectedAssetTypes.size()]));
        preferences.setValue(CONF_KEY_ADT_UUID, configuration.getAdtUUID());

        // Write the DDM templates using the preferences format
        Set<Entry<String, String>> contentTemplates = configuration.getContentTemplates().entrySet();
        for(Entry<String, String> templateMapping : contentTemplates) {
            preferences.setValue(String.format(CONF_KEY_FORMAT_DDM, templateMapping.getKey()), templateMapping.getValue());
        }

        preferences.store();
    }

    @Override
    public void migrateConfiguration(PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {}

}
