package com.savoirfairelinux.flashlight.service.configuration;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.liferay.portal.kernel.util.StringPool;

/**
 * Represents a single "tab" view in the search configuration. All of the tab's configuration is returned as read-only
 * views.
 */
public class FlashlightSearchConfigurationTab {

    public static final int DEFAULT_ORDER = 0;
    public static final int DEFAULT_PAGE_SIZE = 3;
    public static final int DEFAULT_FULL_PAGE_SIZE = 30;
    public static final int DEFAULT_LOAD_MORE_PAGE_SIZE = 3;

    private final String id;
    private final int order;
    private final int pageSize;
    private final int fullPageSize;
    private final int loadMorePageSize;
    private final Map<String, String> titleMap;

    private final String assetType;
    private final String journalArticleViewTemplate;
    private final String sortBy;
    private final boolean sortReverse;
    private final Map<String, String> searchFacets;
    private final Map<String, String> journalArticleTemplates;
    private final Map<String, String> dlFileEntryTypeTemplates;

    /**
     * Creates an empty configuration tab
     */
    public FlashlightSearchConfigurationTab() {
        this(generateId(), DEFAULT_ORDER, DEFAULT_PAGE_SIZE, DEFAULT_FULL_PAGE_SIZE, DEFAULT_LOAD_MORE_PAGE_SIZE, Collections.emptyMap(), StringPool.BLANK, StringPool.BLANK, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), StringPool.BLANK, false);
    }

    /**
     * Creates a new configuration tab with a generated unique ID
     *
     * @param order The tab's appearance order
     * @param pageSize The amount or results to display when no tab is selected
     * @param fullPageSize The amount of results to display when a tab is selected
     * @param loadMorePageSize The amount of results to display per call to "load more"
     * @param titleMap The tab's localized title map
     * @param assetType The tab's asset type
     * @param journalArticleViewTemplate The template used to render a journal article search result in the portlet
     * @param searchFacets The tab's search facets and their JSON configuration, indexed by class name
     * @param journalArticleTemplates The tab's Journal Article display templates
     * @param dlFileEntryTypeTemplates The tab's DL File Entry type display templates
     */
    public FlashlightSearchConfigurationTab(int order, int pageSize, int fullPageSize, int loadMorePageSize, Map<String, String> titleMap, String assetType, String journalArticleViewTemplate, Map<String, String> searchFacets, Map<String, String> journalArticleTemplates, Map<String, String> dlFileEntryTypeTemplates, String sortBy, boolean sortReverse) {
        this(generateId(), order, pageSize, fullPageSize, loadMorePageSize, titleMap, assetType, journalArticleViewTemplate, searchFacets, journalArticleTemplates, dlFileEntryTypeTemplates, sortBy, sortReverse);
    }

    /**
     * Creates a new configuration tab
     *
     * @param id The tab's unique ID
     * @param order The tab's appearance order
     * @param pageSize The amount or results to display when no tab is selected
     * @param fullPageSize The amount of results to display when a tab is selected
     * @param loadMorePageSize The amount of results to display per call to "load more"
     * @param titleMap The tab's localized title map
     * @param assetType The tab's asset type
     * @param journalArticleViewTemplate The template used to render a journal article search result in the portlet
     * @param searchFacets The tab's search facets and their JSON configuration, indexed by class name
     * @param journalArticleTemplates The tab's Journal Article display templates
     * @param dlFileEntryTypeTemplates The tab's DL File Entry type display templates
     */
    public FlashlightSearchConfigurationTab(String id, int order, int pageSize, int fullPageSize, int loadMorePageSize, Map<String, String> titleMap, String assetType, String journalArticleViewTemplate, Map<String, String> searchFacets, Map<String, String> journalArticleTemplates, Map<String, String> dlFileEntryTypeTemplates, String sortBy, boolean sortReverse) {
        this.id = id;
        this.order = order;
        this.pageSize = pageSize;
        this.fullPageSize = fullPageSize;
        this.loadMorePageSize = loadMorePageSize;
        this.titleMap = Collections.unmodifiableMap(titleMap);
        this.assetType = assetType;
        this.journalArticleViewTemplate = journalArticleViewTemplate;
        this.searchFacets = Collections.unmodifiableMap(searchFacets);
        this.journalArticleTemplates = Collections.unmodifiableMap(journalArticleTemplates);
        this.dlFileEntryTypeTemplates = Collections.unmodifiableMap(dlFileEntryTypeTemplates);
        this.sortBy = sortBy;
        this.sortReverse = sortReverse;
    }

    /**
     * @return The tab's unique ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return The tab's appearance order
     */
    public int getOrder() {
        return this.order;
    }

    /**
     * @return The amount of results to display when no tab is selected
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * @return The amount of results to display when a tab is selected
     */
    public int getFullPageSize() {
        return this.fullPageSize;
    }

    /**
     * @return The amount of results to display when calling "load more"
     */
    public int getLoadMorePageSize() {
        return this.loadMorePageSize;
    }

    /**
     * @return The tab's localized title map
     */
    public Map<String, String> getTitleMap() {
        return this.titleMap;
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
    public String getAssetType() {
        return this.assetType;
    }

    /**
     * @return The tab's sort field, or StringPool.BLANK if no sorting is set.
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * @return <c>true</c> to reverse the sort order.
     */
    public boolean isSortReverse() {
        return sortReverse;
    }

    public String getJournalArticleViewTemplate() {
        return this.journalArticleViewTemplate;
    }

    /**
     * @return The search facets that are enabled in this tab, along with their configuration
     */
    public Map<String, String> getSearchFacets() {
        return this.searchFacets;
    }

    /**
     * @return The tab's Journal Article content display templates
     */
    public Map<String, String> getJournalArticleTemplates() {
        return this.journalArticleTemplates;
    }

    /**
     * @return The tab's DL File Entry type display templates
     */
    public Map<String, String> getDLFileEntryTypeTemplates() {
        return this.dlFileEntryTypeTemplates;
    }

    /**
     * @return A unique configuration tab ID
     */
    private static String generateId() {
        return UUID.randomUUID().toString();
    }
}
