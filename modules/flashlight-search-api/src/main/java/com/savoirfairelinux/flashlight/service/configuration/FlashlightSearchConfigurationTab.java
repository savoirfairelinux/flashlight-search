package com.savoirfairelinux.flashlight.service.configuration;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.liferay.portal.kernel.util.StringPool;

/**
 * Represents a single "tab" view in the search configuration. All of the tab's configuration is returned as read-only
 * views.
 */
public class FlashlightSearchConfigurationTab {

    private String id;
    private int order;
    private Map<String, String> titleMap;

    private List<String> assetTypes;
    private Map<String, String> contentTemplates;

    /**
     * Creates an empty configuration tab
     */
    public FlashlightSearchConfigurationTab() {
        this(generateId(), 0, Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
    }

    /**
     * Creates a new configuration tab with a generated unique ID
     *
     * @param order The tab's appearance order
     * @param titleMap The tab's localized title map
     * @param assetTypes the tab's asset types
     * @param contentTemplates The tab's content display templates
     */
    public FlashlightSearchConfigurationTab(int order, Map<String, String> titleMap, List<String> assetTypes, Map<String, String> contentTemplates) {
        this(generateId(), order, titleMap, assetTypes, contentTemplates);
    }

    /**
     * Creates a new configuration tab
     *
     * @param id The tab's unique ID
     * @param order The tab's appearance order
     * @param titleMap The tab's localized title map
     * @param assetTypes The tab's asset types
     * @param contentTemplates The tab's content display templates
     */
    public FlashlightSearchConfigurationTab(String id, int order, Map<String, String> titleMap, List<String> assetTypes, Map<String, String> contentTemplates) {
        this.id = id;
        this.order = order;
        this.titleMap = Collections.unmodifiableMap(titleMap);
        this.assetTypes = Collections.unmodifiableList(assetTypes);
        this.contentTemplates = Collections.unmodifiableMap(contentTemplates);
    }

    /**
     * @return The tab's unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * @return The tab's appearance order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @return The tab's localized title map
     */
    public Map<String, String> getTitleMap() {
        return titleMap;
    }

    /**
     * Returns the tab's title in the given locale or a default value
     * @param locale The locale in which to return the title
     * @return The title in the given locale. If locale not found, will return the title in another locale. If none
     *         found, returns an empty string.
     */
    public String getTitle(Locale locale) {
        return this.getTitle(locale.toString());
    }

    /**
     * Returns the tab's title in the given locale or a default value
     * @param languageId The language in which to return the title
     * @return The title in the given locale. If locale not found, will return the title in another locale. If none
     *         found, returns an empty string.
     */
    public String getTitle(String languageId) {
        return this.titleMap.getOrDefault(languageId, this.titleMap.values().stream().findFirst().orElse(StringPool.BLANK));
    }

    /**
     * @return The tab's selected asset types
     */
    public List<String> getAssetTypes() {
        return assetTypes;
    }

    /**
     * @return The tab's content display templates
     */
    public Map<String, String> getContentTemplates() {
        return contentTemplates;
    }

    /**
     * @return A unique configuration tab ID
     */
    private static String generateId() {
        return UUID.randomUUID().toString();
    }

}
