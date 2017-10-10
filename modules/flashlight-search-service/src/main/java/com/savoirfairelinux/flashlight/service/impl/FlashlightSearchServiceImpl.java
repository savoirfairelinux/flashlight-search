// Copyright 2017 Savoir-faire Linux
// This file is part of Flashlight Search.

// Flashlight Search is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Flashlight Search is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Flashlight Search.  If not, see <http://www.gnu.org/licenses/>.

package com.savoirfairelinux.flashlight.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.model.DLFileEntryTypeConstants;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.*;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.search.hits.HitsProcessorRegistry;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.facet.SearchFacetDisplayHandler;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorage;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorageV1;
import com.savoirfairelinux.flashlight.service.impl.customizer.FlashlightSearchContextCustomizerServiceTracker;
import com.savoirfairelinux.flashlight.service.impl.facet.DDMStructureFacet;
import com.savoirfairelinux.flashlight.service.impl.facet.DLFileEntryTypeFacet;
import com.savoirfairelinux.flashlight.service.impl.facet.displayhandler.SearchFacetDisplayHandlerServiceTracker;
import com.savoirfairelinux.flashlight.service.impl.search.result.SearchResultProcessorServiceTracker;
import com.savoirfairelinux.flashlight.service.impl.search.result.template.DLFileEntryTypeTemplateHandler;
import com.savoirfairelinux.flashlight.service.model.SearchPage;
import com.savoirfairelinux.flashlight.service.model.SearchResult;
import com.savoirfairelinux.flashlight.service.model.SearchResultFacet;
import com.savoirfairelinux.flashlight.service.model.SearchResultsContainer;
import com.savoirfairelinux.flashlight.service.portlet.template.JournalArticleViewTemplateHandler;
import com.savoirfairelinux.flashlight.service.search.result.SearchResultProcessor;
import com.savoirfairelinux.flashlight.service.search.result.exception.SearchResultProcessorException;

@Component(
    service = FlashlightSearchService.class,
    immediate = true
)
public class FlashlightSearchServiceImpl implements FlashlightSearchService {

    private static final Log LOG = LogFactoryUtil.getLog(FlashlightSearchServiceImpl.class);

    private static final List<String> MANAGED_SEARCH_FACETS = Arrays.asList(
        DDMStructureFacet.class.getName(),
        AssetEntriesFacet.class.getName(),
        DLFileEntryTypeFacet.class.getName()
    );

    @Reference
    private ClassNameLocalService classNameService;

    @Reference
    private DLFileEntryTypeLocalService dlFileEntryTypeService;

    @Reference
    private DDMStructureLocalService ddmStructureService;

    @Reference
    private DDMTemplateLocalService ddmTemplateService;

    @Reference
    private GroupLocalService groupService;

    @Reference
    private Portal portal;

    @Reference(unbind = "-")
    private FacetedSearcherManager facetedSearcherManager;

    @Reference
    private SearchResultProcessorServiceTracker searchResultProcessorServicetracker;

    @Reference
    private HitsProcessorRegistry hitsProcessorRegistry;

    @Reference
    private SearchFacetDisplayHandlerServiceTracker searchFacetDisplayHandlerServiceTracker;

    @Reference
    private FlashlightSearchContextCustomizerServiceTracker flashlightSearchContextCustomizerServiceTracker;

    private ConfigurationStorage storageEngine;

    @Activate
    protected void activate() {
        this.storageEngine = new ConfigurationStorageV1();
    }

    @Override
    public FlashlightSearchConfiguration readConfiguration(PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        return this.storageEngine.readConfiguration(preferences);
    }

    @Override
    public void saveGlobalSettings(String adtUuid, boolean doSearchOnStartup, String doSearchOnStartupKeywords, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        this.storageEngine.saveGlobalSettings(adtUuid, doSearchOnStartup, doSearchOnStartupKeywords, preferences);
    }

    @Override
    public void saveConfigurationTab(FlashlightSearchConfigurationTab configurationTab, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        this.storageEngine.saveConfigurationTab(configurationTab, preferences);
    }

    @Override
    public void saveSearchFacetConfig(FlashlightSearchConfigurationTab configurationTab, SearchFacet searchFacet, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        this.storageEngine.saveSearchFacetConfig(configurationTab, searchFacet, preferences);
    }

    @Override
    public void deleteConfigurationTab(String tabId, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        this.storageEngine.deleteConfigurationTab(tabId, preferences);
    }

    @Override
    public Map<String, List<DDMStructure>> getDDMStructures(long groupId) throws PortalException {
        long[] groupIds = this.portal.getCurrentAndAncestorSiteGroupIds(groupId, true);
        HashMap<String, List<DDMStructure>> indexedStructures = new HashMap<>();

        List<DDMStructure> groupStructures =  this.ddmStructureService.getStructures(groupIds);
        for(DDMStructure structure : groupStructures) {
            if(!structure.getTemplates().isEmpty()) {
                String clName = structure.getClassName();
                List<DDMStructure> structures;
                if(!indexedStructures.containsKey(clName)) {
                    structures = new ArrayList<>();
                    indexedStructures.put(clName, structures);
                } else {
                    structures = indexedStructures.get(clName);
                }
                structures.add(structure);
            }
        }

        return indexedStructures;
    }

    @Override
    public Map<Group, List<DDMTemplate>> getJournalArticleViewTemplates(PermissionChecker permissionChecker, long groupId) throws PortalException {
        return this.getApplicationDisplayTemplates(permissionChecker, groupId, this.classNameService.getClassNameId(JournalArticleViewTemplateHandler.class));
    }

    @Override
    public Map<DLFileEntryType, List<DDMTemplate>> getFileEntryTypes(PermissionChecker permissionChecker, long groupId) throws PortalException {
        List<DLFileEntryType> fileEntryTypes = this.dlFileEntryTypeService.getFileEntryTypes(this.portal.getCurrentAndAncestorSiteGroupIds(groupId));
        HashMap<DLFileEntryType, List<DDMTemplate>> fileEntryTypeTemplateMapping = new HashMap<>(fileEntryTypes.size());

        Map<Group, List<DDMTemplate>> templatesByGroup = this.getDLFileEntryTypeTemplates(permissionChecker, groupId);
        List<DDMTemplate> templates = new ArrayList<>();
        for(List<DDMTemplate> groupTemplates : templatesByGroup.values()) {
            templates.addAll(groupTemplates);
        }

        // Put the default file entry type in there (because getFileEntryTypes won't do it)
        fileEntryTypeTemplateMapping.put(this.dlFileEntryTypeService.getDLFileEntryType(DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT), templates);

        for(DLFileEntryType fileEntryType : fileEntryTypes) {
            fileEntryTypeTemplateMapping.put(fileEntryType, templates);
        }

        return fileEntryTypeTemplateMapping;
    }

    @Override
    public Map<Group, List<DDMTemplate>> getPortletApplicationDisplayTemplates(PermissionChecker permissionChecker, long groupId) throws PortalException {
        return this.getApplicationDisplayTemplates(permissionChecker, groupId, this.classNameService.getClassNameId(FlashlightSearchService.ADT_CLASS));
    }

    @Override
    public List<String> getSupportedAssetTypes() {
        return this.searchResultProcessorServicetracker.getSupportedAssetTypes();
    }

    @Override
    public List<SearchFacet> getSupportedSearchFacets() {
        List<SearchFacet> liferayFacets = SearchFacetTracker.getSearchFacets();
        ArrayList<SearchFacet> supportedFacets = new ArrayList<>(liferayFacets.size());
        for(SearchFacet f : liferayFacets) {
            String underlyingFacet = f.getFacetClassName();
            if(underlyingFacet != null && !MANAGED_SEARCH_FACETS.contains(underlyingFacet)) {
                supportedFacets.add(f);
            }
        }
        return supportedFacets;
    }

    @Override
    public String displayTerm(HttpServletRequest request, SearchResultFacet searchResultFacet, String queryTerm) {
        SearchFacetDisplayHandler searchFacetDisplayHandler = searchFacetDisplayHandlerServiceTracker.getSearchFacetDisplayHandlerBySearchFacet(searchResultFacet.getSearchFacetClass());
        if (searchFacetDisplayHandler == null) {
            LOG.info("Could not find any SearchFacetDisplayHandler for SearchFacet [" + searchResultFacet.getSearchFacetClass().getName() + "]");
        }
        return searchFacetDisplayHandler != null ? searchFacetDisplayHandler.displayTerm(request, searchResultFacet.getFacetConfiguration(), queryTerm) : queryTerm;
    }

    @Override
    public SearchResultsContainer search(PortletRequest request, PortletResponse response, String tabId, int pageOffset, boolean isLoadMore) throws ReadOnlyException, ValidatorException, IOException, SearchException {
        FlashlightSearchConfiguration config = this.readConfiguration(request.getPreferences());
        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        FacetedSearcher searcher = this.facetedSearcherManager.createFacetedSearcher();

        LinkedHashMap<FlashlightSearchConfigurationTab, SearchPage> resultMap = new LinkedHashMap<>(tabs.size());
        for(FlashlightSearchConfigurationTab tab : tabs.values()) {
            int pageSize;
            int loadMoreSize;

            if(tab.getId().equals(tabId)) {
                pageSize = tab.getFullPageSize();
            } else {
                pageSize = tab.getPageSize();
            }

            if(isLoadMore) {
                loadMoreSize = tab.getLoadMorePageSize();
            } else {
                loadMoreSize = 0;
            }

            resultMap.put(tab, this.search(request, response, config, tab, searcher, pageOffset, pageSize, loadMoreSize));
        }

        return new SearchResultsContainer(resultMap);
    }

    /**
     * Returns the DL File Entry types templates
     *
     * @param permissionChecker The current context's permission checker
     * @param groupId The current site ID
     * @return A list of templates indexed by file entry types
     *
     * @throws PortalException If an error occurs while searching templates
     */
    private Map<Group, List<DDMTemplate>> getDLFileEntryTypeTemplates(PermissionChecker permissionChecker, long groupId) throws PortalException {
        return this.getApplicationDisplayTemplates(permissionChecker, groupId, this.classNameService.getClassNameId(DLFileEntryTypeTemplateHandler.class));
    }

    /**
     * Returns the DL File Entry types templates
     *
     * @param permissionChecker The current context's permission checker
     * @param groupId The current site ID
     * @param classNameId The template's classNameId
     * @return A list of templates indexed by file entry types
     *
     * @throws PortalException If an error occurs while searching templates
     */
    private Map<Group, List<DDMTemplate>> getApplicationDisplayTemplates(PermissionChecker permissionChecker, long groupId, long classNameId) throws PortalException {
        HashMap<Group, List<DDMTemplate>> adts = new HashMap<>();

        long[] currentGroupIds = this.portal.getCurrentAndAncestorSiteGroupIds(groupId);
        long userId = permissionChecker.getUserId();
        for(long currentGroupId : currentGroupIds) {
            List<DDMTemplate> groupTemplates = this.ddmTemplateService.getTemplates(currentGroupId, classNameId)
                .stream()
                .filter(template -> {
                    // See DDMTemplatePermission.java in Liferay's source code for the inspirational stuff
                    String modelResourceName = DDMTemplate.class.getName();
                    long companyId = template.getCompanyId();
                    long templateId = template.getTemplateId();
                    String actionKey = ActionKeys.VIEW;

                    return (
                        permissionChecker.hasOwnerPermission(companyId, modelResourceName, templateId, userId, actionKey) ||
                        permissionChecker.hasPermission(companyId, modelResourceName, templateId, actionKey)
                    );
                })
                .collect(Collectors.toList());

            // If we have templates to show, put it in the map
            if(!groupTemplates.isEmpty()) {
                Group group = this.groupService.getGroup(currentGroupId);
                adts.put(group, groupTemplates);
            }
        }

        return adts;
    }

    /**
     * Performs a search for a single configuration tab
     *
     * @param request The portlet request that triggered the search
     * @param response The portlet response that triggered the search
     * @param config The search configuration
     * @param tab The tab in which the search is performed
     * @param searcher The searched used for the search itself
     * @param offset The number of pages to skip
     * @param pageSize The size of a full page
     * @param loadMoreSize The size of a "load more". Send 0 if you are not performing the continuation of a previous search
     *
     * @return A search page
     *
     * @throws SearchException If an error occurs during search
     */
    private SearchPage search(PortletRequest request, PortletResponse response, FlashlightSearchConfiguration config, FlashlightSearchConfigurationTab tab, FacetedSearcher searcher, int offset, int pageSize, int loadMoreSize) throws SearchException {
        String selectedAssetType = tab.getAssetType();
        SearchContext searchContext = SearchContextFactory.getInstance(this.portal.getHttpServletRequest(request));

        // Note for future reference : * Start is the index, starting at 0, of the first result to return.
        //                             * End is the number of results to show, starting at the given index
        int start;
        int end;

        if(loadMoreSize <= 0) {
            start = pageSize * offset;
            end = start + pageSize;
        } else {
            // Load mores cannot start at an offset lesser than 1
            if(offset < 1) {
                offset = 1;
            }
            start = pageSize + ((offset - 1) * loadMoreSize);
            end = start + loadMoreSize;
        }

        if(start < 0) {
            start = 0;
        }

        if(end < 0) {
            end = 0;
        }

        if(end < start) {
            end = start;
        }

        // Configure asset types and search
        searchContext.setStart(start);
        searchContext.setEnd(end);

        // Set keyword to "all" when no keywords are sent. Otherwize, Liferay might return the whole index.
        if(Validator.isNull(searchContext.getKeywords())) {
            searchContext.setKeywords(FlashlightSearchService.CONFIGURATION_DEFAULT_SEARCH_KEYWORDS);
        }

        String[] assetEntryClassNames = new String[1];
        assetEntryClassNames[0] = selectedAssetType;
        searchContext.setEntryClassNames(assetEntryClassNames);

        AssetEntriesFacet assetEntriesFacet = new AssetEntriesFacet(searchContext);
        searchContext.addFacet(assetEntriesFacet);

        SearchResultProcessor processor = this.searchResultProcessorServicetracker.getSearchResultProcessor(selectedAssetType);
        if(processor != null) {
            Collection<Facet> facets = processor.getFacets(searchContext, config, tab);
            for (Facet facet : facets) {
                searchContext.addFacet(facet);
            }
        }

        this.addConfiguredFacets(searchContext, tab);
        this.setSorting(searchContext, tab);

        this.flashlightSearchContextCustomizerServiceTracker.applyCustomizers(request, response, config, tab, searchContext);

        Hits hits = searcher.search(searchContext);
        this.hitsProcessorRegistry.process(searchContext, hits);

        List<SearchResult> searchResults = new ArrayList<>(end - start);

        for (Document document : hits.getDocs()) {
            String assetType = document.getField(Field.ENTRY_CLASS_NAME).getValue();
            SearchResult processedDocument;

            try {
                processedDocument = this.processDocument(document, assetType, request, response, searchContext, config, tab);
            } catch (SearchResultProcessorException e) {
                LOG.error("Cannot process document", e);
                processedDocument = null;
            }

            if(processedDocument != null) {
                searchResults.add(processedDocument);
            } else {
                LOG.debug("Search result omitted as it was null");
            }
        }

        return new SearchPage(searchResults, hits.getLength(), getConfiguredFacets(searchContext));
    }

    /**
     * Setup sorting, if confirured for this tab.
     * @param searchContext the current SearchContext.
     * @param tab the current tab configuration.
     */
    private void setSorting(SearchContext searchContext, FlashlightSearchConfigurationTab tab) {
        if (!StringPool.BLANK.equals(tab.getSortBy())) {
            searchContext.setSorts(new Sort(tab.getSortBy() + "_sortable", tab.isSortReverse()));
        }
    }

    /**
     * Get the list of SearchFacet from a searchContext (containing Facets).
     * @param searchContext an initialized SearchContext.
     * @return the list of SearchFacet matching searchContext.getFacets().
     */
    private List<SearchResultFacet> getConfiguredFacets(SearchContext searchContext) {
        Set<String> searchResultFacetsFieldNames = searchContext.getFacets().values().stream()
            .map(facet -> facet.getFieldName())
            .collect(Collectors.toSet());
        return this.getSupportedSearchFacets().stream()
            .filter(searchFacet -> searchResultFacetsFieldNames.contains(searchFacet.getFieldName()))
            .map(SearchResultFacet::new)
            .collect(Collectors.toList());
    }

    /**
     * Add the facets from a tab configuration to the actual search request.
     *
     * @param searchContext the SearchContext to which the facets will be added.
     * @param tab           the current search tab.
     */
    private void addConfiguredFacets(SearchContext searchContext, FlashlightSearchConfigurationTab tab) {
        this.getSupportedSearchFacets().stream()
            .filter(searchFacet -> tab.getSearchFacets().keySet().contains(searchFacet.getClassName()))
            .map(searchFacet -> {
                try {
                    // See com.liferay.portal.search.web.internal.portlet.action.SearchConfigurationAction.processAction()
                    // This formats the facet configuration to a JSON format suitable to
                    // com.liferay.portal.search.web.facet.SearchFacet.init()

                    JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
                    JSONArray facetsJSONArray = JSONFactoryUtil.createJSONArray();

                    JSONObject facetJSONObject = JSONFactoryUtil.createJSONObject();

                    facetJSONObject.put("className", searchFacet.getFacetClassName());
                    facetJSONObject.put("data", JSONFactoryUtil.createJSONObject(tab.getSearchFacets().get(searchFacet.getClassName())));
                    facetJSONObject.put("fieldName", searchFacet.getFieldName());
                    facetJSONObject.put("id", searchFacet.getId());
                    facetJSONObject.put("label", searchFacet.getLabel());
                    facetJSONObject.put("order", searchFacet.getOrder());

                    //boolean displayFacet = ParamUtil.getBoolean(actionRequest, searchFacet.getClassName() + "displayFacet");
                    boolean displayFacet = true;

                    facetJSONObject.put("static", !displayFacet);

                    //double weight = ParamUtil.getDouble(actionRequest, searchFacet.getClassName() + "weight");
                    double weight = 1;

                    facetJSONObject.put("weight", weight);

                    facetsJSONArray.put(facetJSONObject);

                    jsonObject.put("facets", facetsJSONArray);

                    searchFacet.init(searchContext.getCompanyId(), jsonObject.toString(), searchContext);
                    return searchFacet.getFacet();
                } catch (Exception e) {
                    LOG.warn("Could not initialize search facet [" + searchFacet.getClassName() + "]", e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .forEach(searchContext::addFacet);
    }

    /**
     * Transforms a document in a search result
     *
     * @param document The document itself
     * @param assetType The document's asset type
     * @param request The portlet request that triggered the search
     * @param response The portlet response that triggered the search
     * @param searchContext The search context
     * @param configuration The search configuration
     * @param tab The tab in which the search is performed
     *
     * @return A search result or null if no processor can handle the document
     *
     * @throws SearchResultProcessorException Thrown if an active processor was unable to process the document
     */
    private SearchResult processDocument(Document document, String assetType, PortletRequest request, PortletResponse response, SearchContext searchContext, FlashlightSearchConfiguration configuration, FlashlightSearchConfigurationTab tab) throws SearchResultProcessorException {
        SearchResultProcessor processor = this.searchResultProcessorServicetracker.getSearchResultProcessor(assetType);
        SearchResult result;

        if(processor != null) {
            result = processor.process(document, request, response, searchContext, tab);
        } else {
            result = null;
        }

        return result;
    }

}
