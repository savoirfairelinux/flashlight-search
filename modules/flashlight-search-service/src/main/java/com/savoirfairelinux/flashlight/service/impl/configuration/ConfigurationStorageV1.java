package com.savoirfairelinux.flashlight.service.impl.configuration;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.util.PatternConstants;

import static java.lang.String.format;

/**
 * First version of the configuration storage mechanism
 */
public class ConfigurationStorageV1 implements ConfigurationStorage {

    private static final String CONF_KEY_ADT_UUID = "adt-uuid";
    private static final String CONF_KEY_DO_SEARCH_ON_STARTUP = "do-search-on-startup";
    private static final String CONF_KEY_TABS = "tabs";

    private static final String CONF_KEY_FORMAT_ORDER = "%s[order]";
    private static final String CONF_KEY_FORMAT_PAGE_SIZE = "%s[page-size]";
    private static final String CONF_KEY_FORMAT_FULL_PAGE_SIZE = "%s[full-page-size]";
    private static final String CONF_KEY_FORMAT_LOAD_MORE_PAGE_SIZE = "%s[load-more-page-size]";
    private static final String CONF_KEY_FORMAT_ASSET_TYPE = "%s[asset-type]";
    private static final String CONF_KEY_FORMAT_JOURNAL_ARTICLE_VIEW_TEMPLATE = "%s[journal-article-view-template]";

    private static final String CONF_KEY_FORMAT_SEARCH_FACET = "%s[search-facet-%s]";
    private static final String CONF_KEY_FORMAT_JOURNAL_ARTICLE_TEMPLATE = "%s[journal-article-template-%s]";
    private static final String CONF_KEY_FORMAT_DL_FILE_ENTRY_TYPE_TEMPLATE = "%s[dl-file-entry-type-template-%s]";
    private static final String CONF_KEY_FORMAT_TITLE = "%s[title-%s]";

    private static final String CONF_KEY_FORMAT_SORT_BY = "%s[sort-by]";
    private static final String CONF_KEY_FORMAT_SORT_REVERSE = "%s[sort-reverse]";

    private static final String CONF_KEY_PATTERN_FORMAT_JOURNAL_ARTICLE_TEMPLATES = "^%s\\[journal-article-template-" + PatternConstants.UUID + "\\]$";
    private static final int CONF_KEY_PATTERN_FORMAT_JOURNAL_ARTICLE_TEMPLATES_GROUP_UUID = 1;

    private static final String CONF_KEY_PATTERN_FORMAT_DL_FILE_ENTRY_TYPE_TEMPLATES = "^%s\\[dl-file-entry-type-template-" + PatternConstants.UUID + "\\]$";
    private static final int CONF_KEY_PATTERN_FORMAT_DL_FILE_ENTRY_TYPE_TEMPLATES_GROUP_UUID = 1;

    private static final String CONF_KEY_PATTERN_FORMAT_TITLE = "^%s\\[title-" + PatternConstants.LOCALE + "\\]$";
    private static final int CONF_KEY_PATTERN_FORMAT_TITLE_GROUP_LOCALE = 1;

    private static final String CONF_KEY_PATTERN_FORMAT_SEARCH_FACET = "^%s\\[search-facet-" + PatternConstants.CLASS_NAME + "\\]$";
    private static final int CONF_KEY_PATTERN_FORMAT_SEARCH_FACET_GROUP_CLASS_NAME = 1;

    private static final String ZERO = "0";
    private static final String THREE = "3";
    private static final String[] EMPTY_ARRAY = new String[0];

    @Override
    public FlashlightSearchConfiguration readConfiguration(PortletPreferences preferences) {
        // Get the ADT UUID
        String adtUUID = preferences.getValue(CONF_KEY_ADT_UUID, StringPool.BLANK);

        // Perform a search on startup?
        boolean doSearchOnStartup = preferences.getValue(CONF_KEY_DO_SEARCH_ON_STARTUP, StringPool.FALSE).equals(StringPool.TRUE);

        // Get the tabs
        String[] tabIds = preferences.getValues(CONF_KEY_TABS, EMPTY_ARRAY);
        int tabIdsLength = tabIds.length;
        ArrayList<FlashlightSearchConfigurationTab> tabs = new ArrayList<>(tabIdsLength);

        // For each tabs, get its configuration
        for(int i = 0; i < tabIdsLength; i++) {
            tabs.add(this.readTabConfiguration(preferences, tabIds[i]));
        }

        return new FlashlightSearchConfiguration(adtUUID, doSearchOnStartup, tabs);
    }

    @Override
    public void saveGlobalSettings(String adtUuid, boolean doSearchOnStartup, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        preferences.setValue(CONF_KEY_ADT_UUID, adtUuid);
        preferences.setValue(CONF_KEY_DO_SEARCH_ON_STARTUP, Boolean.toString(doSearchOnStartup));
        preferences.store();
    }

    @Override
    public void saveConfigurationTab(FlashlightSearchConfigurationTab configurationTab, PortletPreferences preferences) throws IOException, ValidatorException, ReadOnlyException {
        String tabId = configurationTab.getId();
        int order = configurationTab.getOrder();
        int pageSize = configurationTab.getPageSize();
        int fullPageSize = configurationTab.getFullPageSize();
        int loadMorePageSize = configurationTab.getLoadMorePageSize();
        String sortBy = configurationTab.getSortBy();
        boolean sortReverse = configurationTab.isSortReverse();

        String assetType = configurationTab.getAssetType();
        String journalArticleViewTemplate = configurationTab.getJournalArticleViewTemplate();
        Map<String, String> searchFacets = configurationTab.getSearchFacets();
        Map<String, String> titleMap = configurationTab.getTitleMap();
        Map<String, String> journalArticleTemplates = configurationTab.getJournalArticleTemplates();
        Map<String, String> dlFileEntryTemplates = configurationTab.getDLFileEntryTypeTemplates();

        // Write singular values
        String assetTypeKey = format(CONF_KEY_FORMAT_ASSET_TYPE, tabId);
        String journalArticleViewTemplateKey = format(CONF_KEY_FORMAT_JOURNAL_ARTICLE_VIEW_TEMPLATE, tabId);
        String orderKey = format(CONF_KEY_FORMAT_ORDER, tabId);
        String pageSizeKey = format(CONF_KEY_FORMAT_PAGE_SIZE, tabId);
        String fullPageSizeKey = format(CONF_KEY_FORMAT_FULL_PAGE_SIZE, tabId);
        String loadMorePageSizeKey = format(CONF_KEY_FORMAT_LOAD_MORE_PAGE_SIZE, tabId);
        String sortByKey = format(CONF_KEY_FORMAT_SORT_BY, tabId);
        String sortReverseKey = format(CONF_KEY_FORMAT_SORT_REVERSE, tabId);

        preferences.setValue(assetTypeKey, assetType);
        preferences.setValue(journalArticleViewTemplateKey, journalArticleViewTemplate);
        preferences.setValue(orderKey, Integer.toString(order));
        preferences.setValue(pageSizeKey, Integer.toString(pageSize));
        preferences.setValue(fullPageSizeKey, Integer.toString(fullPageSize));
        preferences.setValue(loadMorePageSizeKey, Integer.toString(loadMorePageSize));
        preferences.setValue(sortByKey, sortBy);
        preferences.setValue(sortReverseKey, Boolean.toString(sortReverse));


        // Flush previously entered composed values
        Enumeration<String> prefKeys = preferences.getNames();
        Pattern journalArticleTemplateKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_JOURNAL_ARTICLE_TEMPLATES, tabId));
        Pattern dlFileEntryTemplateKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_DL_FILE_ENTRY_TYPE_TEMPLATES, tabId));
        Pattern titleKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_TITLE, tabId));
        Pattern searchFacetConfigPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_SEARCH_FACET, tabId));
        while(prefKeys.hasMoreElements()) {
            String key = prefKeys.nextElement();
            Matcher jouranlArticleTemplateMatcher = journalArticleTemplateKeyPattern.matcher(key);
            Matcher dlFileEntryTemplateMatcher = dlFileEntryTemplateKeyPattern.matcher(key);
            Matcher titleMatcher = titleKeyPattern.matcher(key);
            Matcher searchFacetConfigMatcher = searchFacetConfigPattern.matcher(key);

            if(jouranlArticleTemplateMatcher.matches() || dlFileEntryTemplateMatcher.matches() || titleMatcher.matches()) {
                // Always re-write DDM and title fields
                preferences.reset(key);
            } else if(searchFacetConfigMatcher.matches()) {
                // Only flush a facet configuration if it was removed from the list
                String facetClassName = searchFacetConfigMatcher.group(CONF_KEY_PATTERN_FORMAT_SEARCH_FACET_GROUP_CLASS_NAME);
                if(!searchFacets.containsKey(facetClassName)) {
                    preferences.reset(key);
                }
            }
        }

        // Write the Journal Article display templates
        for(Entry<String, String> journalTemplateMapping : journalArticleTemplates.entrySet()) {
            preferences.setValue(format(CONF_KEY_FORMAT_JOURNAL_ARTICLE_TEMPLATE, tabId, journalTemplateMapping.getKey()), journalTemplateMapping.getValue());
        }

        // Write the DL File Entry display templates
        for(Entry<String, String> dlTemplateMapping : dlFileEntryTemplates.entrySet()) {
            preferences.setValue(format(CONF_KEY_FORMAT_DL_FILE_ENTRY_TYPE_TEMPLATE, tabId, dlTemplateMapping.getKey()), dlTemplateMapping.getValue());
        }

        // Write the search facets
        for(Entry<String, String> searchFacet : searchFacets.entrySet()) {
            String confKey = format(CONF_KEY_FORMAT_SEARCH_FACET, tabId, searchFacet.getKey());
            // Only write configuration is it was empty before
            if(preferences.getValue(confKey, StringPool.BLANK).isEmpty()) {
                preferences.setValue(confKey, searchFacet.getValue());
            }
        }

        // Write the localized titles
        for(Entry<String, String> titleMapping : titleMap.entrySet()) {
            preferences.setValue(format(CONF_KEY_FORMAT_TITLE, tabId, titleMapping.getKey()), titleMapping.getValue());
        }

        // Register the tab in the tab list, if it is not there already
        String[] registeredTabs = preferences.getValues(CONF_KEY_TABS, EMPTY_ARRAY);
        int registeredTabsLength = registeredTabs.length;
        boolean tabRegistered = false;
        for(int i = 0; !tabRegistered && i < registeredTabsLength; i++) {
            tabRegistered = registeredTabs[i].equals(tabId);
        }

        if(!tabRegistered) {
            registeredTabs = Arrays.copyOf(registeredTabs, registeredTabsLength + 1);
            registeredTabs[registeredTabsLength] = tabId;
            preferences.setValues(CONF_KEY_TABS, registeredTabs);
        }

        preferences.store();
    }

    @Override
    public void saveSearchFacetConfig(FlashlightSearchConfigurationTab configurationTab, SearchFacet searchFacet, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        String tabId = configurationTab.getId();
        String facetConfigKey = format(CONF_KEY_FORMAT_SEARCH_FACET, tabId, searchFacet.getClass().getName());
        preferences.setValue(facetConfigKey, searchFacet.getData().toJSONString());
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
        String assetTypeKey = format(CONF_KEY_FORMAT_ASSET_TYPE, tabId);
        String journalArticleViewTemplateKey = format(CONF_KEY_FORMAT_JOURNAL_ARTICLE_VIEW_TEMPLATE, tabId);
        String orderKey = format(CONF_KEY_FORMAT_ORDER, tabId);
        String pageSizeKey = format(CONF_KEY_FORMAT_PAGE_SIZE, tabId);
        String fullPageSizeKey = format(CONF_KEY_FORMAT_FULL_PAGE_SIZE, tabId);
        String loadMoreSizeKey = format(CONF_KEY_FORMAT_LOAD_MORE_PAGE_SIZE, tabId);
        String sortByKey = format(CONF_KEY_FORMAT_SORT_BY, tabId);
        String sortReverseKey = format(CONF_KEY_FORMAT_SORT_REVERSE, tabId);

        preferences.reset(orderKey);
        preferences.reset(assetTypeKey);
        preferences.reset(journalArticleViewTemplateKey);
        preferences.reset(pageSizeKey);
        preferences.reset(fullPageSizeKey);
        preferences.reset(loadMoreSizeKey);
        preferences.reset(sortByKey);
        preferences.reset(sortReverseKey);

        // Finally, flush out composed values
        Enumeration<String> prefKeys = preferences.getNames();
        Pattern journalArticleTemplatesKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_JOURNAL_ARTICLE_TEMPLATES, tabId));
        Pattern dlFileEntryTemplatesKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_DL_FILE_ENTRY_TYPE_TEMPLATES, tabId));
        Pattern titleKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_TITLE, tabId));
        Pattern searchFacetPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_SEARCH_FACET, tabId));

        while(prefKeys.hasMoreElements()) {
            String key = prefKeys.nextElement();
            boolean journalArticleTemplatesMatches = journalArticleTemplatesKeyPattern.matcher(key).matches();
            boolean dlFileEntryTemplatesMatches = dlFileEntryTemplatesKeyPattern.matcher(key).matches();
            boolean titleMatches = titleKeyPattern.matcher(key).matches();
            boolean searchFacetMatches = searchFacetPattern.matcher(key).matches();

            if(journalArticleTemplatesMatches || dlFileEntryTemplatesMatches || titleMatches || searchFacetMatches) {
                preferences.reset(key);
            }
        }

        preferences.store();
    }

    /**
     * Reads a single configuration tab from the preferences
     *
     * @param preferences The portlet preferences
     * @param tabId The tab's unique ID
     * @return The configuration tab
     */
    private FlashlightSearchConfigurationTab readTabConfiguration(PortletPreferences preferences, String tabId) {
        String orderKey = format(CONF_KEY_FORMAT_ORDER, tabId);
        String pageSizeKey = format(CONF_KEY_FORMAT_PAGE_SIZE, tabId);
        String fullPageSizeKey = format(CONF_KEY_FORMAT_FULL_PAGE_SIZE, tabId);
        String loadMoreSizeKey = format(CONF_KEY_FORMAT_LOAD_MORE_PAGE_SIZE, tabId);
        String assetTypesKey = format(CONF_KEY_FORMAT_ASSET_TYPE, tabId);
        String journalArticleViewTemplateKey = format(CONF_KEY_FORMAT_JOURNAL_ARTICLE_VIEW_TEMPLATE, tabId);
        String sortByKey = format(CONF_KEY_FORMAT_SORT_BY, tabId);
        String sortReverseKey = format(CONF_KEY_FORMAT_SORT_REVERSE, tabId);
        Pattern searchFacetPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_SEARCH_FACET, tabId));
        Pattern journalArticleTemplatesPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_JOURNAL_ARTICLE_TEMPLATES, tabId));
        Pattern dlFileEntryTemplatesPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_DL_FILE_ENTRY_TYPE_TEMPLATES, tabId));
        Pattern titleKeyPattern = Pattern.compile(format(CONF_KEY_PATTERN_FORMAT_TITLE, tabId));

        // Singular keys
        int order = Integer.parseInt(preferences.getValue(orderKey, ZERO));
        int pageSize = Integer.parseInt(preferences.getValue(pageSizeKey, THREE));
        int fullPageSize = Integer.parseInt(preferences.getValue(fullPageSizeKey, String.valueOf(FlashlightSearchConfigurationTab.DEFAULT_FULL_PAGE_SIZE)));
        int loadMorePageSize = Integer.parseInt(preferences.getValue(loadMoreSizeKey, String.valueOf(FlashlightSearchConfigurationTab.DEFAULT_LOAD_MORE_PAGE_SIZE)));
        String assetType = preferences.getValue(assetTypesKey, StringPool.BLANK);
        String journalArticleViewTemplate = preferences.getValue(journalArticleViewTemplateKey, StringPool.BLANK);
        String sortBy = preferences.getValue(sortByKey, StringPool.BLANK);
        boolean sortReverse = Boolean.parseBoolean(preferences.getValue(sortReverseKey, StringPool.FALSE));

        // Composed keys
        Enumeration<String> prefKeys = preferences.getNames();
        HashMap<String, String> journalArticleTemplates = new HashMap<>();
        HashMap<String, String> dlFileEntryTemplates = new HashMap<>();
        HashMap<String, String> searchFacets = new HashMap<>();
        HashMap<String, String> titleMap = new HashMap<>();

        while(prefKeys.hasMoreElements()) {
            String key = prefKeys.nextElement();
            Matcher journalArticleTemplateKeyMatcher = journalArticleTemplatesPattern.matcher(key);
            Matcher dlFileEntryTemplateKeyMatcher = dlFileEntryTemplatesPattern.matcher(key);
            Matcher titleKeyMatcher = titleKeyPattern.matcher(key);
            Matcher searchFacetMatcher = searchFacetPattern.matcher(key);

            if(journalArticleTemplateKeyMatcher.matches()) {
                journalArticleTemplates.put(journalArticleTemplateKeyMatcher.group(CONF_KEY_PATTERN_FORMAT_JOURNAL_ARTICLE_TEMPLATES_GROUP_UUID), preferences.getValue(key, StringPool.BLANK));
            } else if(dlFileEntryTemplateKeyMatcher.matches()) {
                dlFileEntryTemplates.put(dlFileEntryTemplateKeyMatcher.group(CONF_KEY_PATTERN_FORMAT_DL_FILE_ENTRY_TYPE_TEMPLATES_GROUP_UUID), preferences.getValue(key, StringPool.BLANK));
            } else if(titleKeyMatcher.matches()) {
                titleMap.put(titleKeyMatcher.group(CONF_KEY_PATTERN_FORMAT_TITLE_GROUP_LOCALE), preferences.getValue(key, StringPool.BLANK));
            } else if(searchFacetMatcher.matches()) {
                searchFacets.put(searchFacetMatcher.group(CONF_KEY_PATTERN_FORMAT_SEARCH_FACET_GROUP_CLASS_NAME), preferences.getValue(key, StringPool.BLANK));
            }

        }

        return new FlashlightSearchConfigurationTab(tabId, order, pageSize, fullPageSize, loadMorePageSize, titleMap, assetType, journalArticleViewTemplate, searchFacets, journalArticleTemplates, dlFileEntryTemplates, sortBy, sortReverse);
    }

}
