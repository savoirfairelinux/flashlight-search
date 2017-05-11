package com.savoirfairelinux.flashlight.service.impl.configuration;

import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import com.liferay.portal.kernel.util.StringPool;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.util.PatternConstants;

/**
 * First version of the configuration storage mechanism
 */
public class ConfigurationStorageV1 implements ConfigurationStorage {

    private static final String CONF_KEY_ADT_UUID = "adt-uuid";
    private static final String CONF_KEY_TABS = "tabs";

    private static final String CONF_KEY_FORMAT_ORDER = "%s[order]";
    private static final String CONF_KEY_FORMAT_ASSET_TYPES = "%s[asset-types]";

    private static final String CONF_KEY_FORMAT_DDM = "%s[ddm-%s]";
    private static final String CONF_KEY_FORMAT_TITLE = "%s[title-%s]";

    private static final String CONF_KEY_PATTERN_FORMAT_DDM = "^%s\\[ddm-" + PatternConstants.UUID + "\\]$";
    private static final int CONF_KEY_PATTERN_FORMAT_DDM_GROUP_UUID = 1;

    private static final String CONF_KEY_PATTERN_FORMAT_TITLE = "^%s\\[title-" + PatternConstants.LOCALE + "\\]$";
    private static final int CONF_KEY_PATTERN_FORMAT_TITLE_GROUP_LOCALE = 1;

    private static final String ZERO = "0";
    private static final String[] EMPTY_ARRAY = new String[0];

    @Override
    public FlashlightSearchConfiguration readConfiguration(PortletPreferences preferences) {
        // Get the ADT UUID
        String adtUUID = preferences.getValue(CONF_KEY_ADT_UUID, StringPool.BLANK);

        // Get the tabs
        String[] tabIds = preferences.getValues(CONF_KEY_TABS, EMPTY_ARRAY);
        int tabIdsLength = tabIds.length;
        ArrayList<FlashlightSearchConfigurationTab> tabs = new ArrayList<>(tabIdsLength);

        // For each tabs, get its configuration
        for(int i = 0; i < tabIdsLength; i++) {
            tabs.add(this.readTabConfiguration(preferences, tabIds[i]));
        }

        return new FlashlightSearchConfiguration(adtUUID, tabs);
    }

    @Override
    public void saveADT(String adtUuid, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        preferences.setValue(CONF_KEY_ADT_UUID, adtUuid);
        preferences.store();
    }

    @Override
    public void saveConfigurationTab(FlashlightSearchConfigurationTab configurationTab, PortletPreferences preferences) throws IOException, ValidatorException, ReadOnlyException {
        String tabId = configurationTab.getId();
        int order = configurationTab.getOrder();
        List<String> assetTypes = configurationTab.getAssetTypes();
        Map<String, String> titleMap = configurationTab.getTitleMap();
        Map<String, String> contentTemplates = configurationTab.getContentTemplates();

        // Write singular values
        String assetTypesKey = format(CONF_KEY_FORMAT_ASSET_TYPES, tabId);
        String orderKey = format(CONF_KEY_FORMAT_ORDER, tabId);
        preferences.setValues(assetTypesKey, assetTypes.toArray(new String[assetTypes.size()]));
        preferences.setValue(orderKey, Integer.toString(order));

        // Flush previously entered composed values
        Enumeration<String> prefKeys = preferences.getNames();
        Pattern ddmKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_DDM, tabId));
        Pattern titleKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_TITLE, tabId));
        while(prefKeys.hasMoreElements()) {
            String key = prefKeys.nextElement();
            boolean matchesDdm = ddmKeyPattern.matcher(key).matches();
            boolean matchesTitle = titleKeyPattern.matcher(key).matches();
            if(matchesDdm || matchesTitle) {
                preferences.reset(key);
            }
        }

        // Write the DDM templates
        for(Entry<String, String> templateMapping : contentTemplates.entrySet()) {
            preferences.setValue(format(CONF_KEY_FORMAT_DDM, tabId, templateMapping.getKey()), templateMapping.getValue());
        }

        // Write the localized titles
        for(Entry<String, String> titleMapping : titleMap.entrySet()) {
            preferences.setValue(format(CONF_KEY_FORMAT_TITLE, tabId, titleMapping.getKey()), titleMapping.getValue());
        }

        preferences.store();
    }

    @Override
    public void deleteConfigurationTab(String tabId, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        // First, delete any reference to this tab
        List<String> tabIds = Arrays.asList(preferences.getValues(CONF_KEY_TABS, EMPTY_ARRAY))
            .stream()
            .filter(tab -> !tab.equals(tabId))
            .collect(Collectors.toList());
        preferences.setValues(CONF_KEY_TABS, tabIds.toArray(new String[tabIds.size()]));

        // Then, flush out any singular value
        String assetTypesKey = format(CONF_KEY_FORMAT_ASSET_TYPES, tabId);
        String orderKey = format(CONF_KEY_FORMAT_ORDER, tabId);

        preferences.reset(orderKey);
        preferences.reset(assetTypesKey);

        // Finally, flush out composed values
        Enumeration<String> prefKeys = preferences.getNames();
        Pattern ddmKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_DDM, tabId));
        Pattern titleKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_TITLE, tabId));
        while(prefKeys.hasMoreElements()) {
            String key = prefKeys.nextElement();
            boolean ddmMatches = ddmKeyPattern.matcher(key).matches();
            boolean titleMatches = titleKeyPattern.matcher(key).matches();

            if(ddmMatches || titleMatches) {
                preferences.reset(key);
            }
        }

        preferences.store();
    }

    @Override
    public void migrateConfiguration(PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {}

    /**
     * Reads a single configuration tab from the preferences
     *
     * @param preferences The portlet preferences
     * @param tabId The tab's unique ID
     * @return The configuration tab
     */
    private FlashlightSearchConfigurationTab readTabConfiguration(PortletPreferences preferences, String tabId) {
        String orderKey = format(CONF_KEY_FORMAT_ORDER, tabId);
        String assetTypesKey = format(CONF_KEY_FORMAT_ASSET_TYPES, tabId);
        Pattern ddmKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_DDM, tabId));
        Pattern titleKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_TITLE, tabId));

        // Singular keys
        int order = Integer.parseInt(preferences.getValue(orderKey, ZERO));
        List<String> assetTypes = Arrays.asList(preferences.getValues(assetTypesKey, EMPTY_ARRAY));

        // Composed keys
        Enumeration<String> prefKeys = preferences.getNames();
        HashMap<String, String> contentTemplates = new HashMap<>();
        HashMap<String, String> titleMap = new HashMap<>();
        while(prefKeys.hasMoreElements()) {
            String key = prefKeys.nextElement();
            Matcher ddmKeyMatcher = ddmKeyPattern.matcher(key);
            Matcher titleKeyMatcher = titleKeyPattern.matcher(key);

            if(ddmKeyMatcher.matches()) {
                contentTemplates.put(ddmKeyMatcher.group(CONF_KEY_PATTERN_FORMAT_DDM_GROUP_UUID), preferences.getValue(key, StringPool.BLANK));
            } else if(titleKeyMatcher.matches()) {
                titleMap.put(titleKeyMatcher.group(CONF_KEY_PATTERN_FORMAT_TITLE_GROUP_LOCALE), preferences.getValue(key, StringPool.BLANK));
            }

        }

        return new FlashlightSearchConfigurationTab(tabId, order, titleMap, assetTypes, contentTemplates);
    }

}
