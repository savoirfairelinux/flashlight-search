package com.savoirfairelinux.flashlight.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.search.hits.HitsProcessorRegistry;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.facet.SearchFacetDisplayHandler;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorage;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorageV1;
import com.savoirfairelinux.flashlight.service.impl.facet.DDMStructureFacet;
import com.savoirfairelinux.flashlight.service.impl.facet.displayhandler.SearchFacetDisplayHandlerServiceTracker;
import com.savoirfairelinux.flashlight.service.impl.search.result.SearchResultProcessorServiceTracker;
import com.savoirfairelinux.flashlight.service.model.SearchPage;
import com.savoirfairelinux.flashlight.service.model.SearchResult;
import com.savoirfairelinux.flashlight.service.model.SearchResultFacet;
import com.savoirfairelinux.flashlight.service.model.SearchResultsContainer;
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
        AssetEntriesFacet.class.getName()
    );

    @Reference(unbind = "-")
    private ClassNameLocalService classNameService;

    @Reference(unbind = "-")
    private DDMStructureLocalService ddmStructureService;

    @Reference(unbind = "-")
    private DDMTemplateLocalService ddmTemplateService;

    @Reference(unbind = "-")
    private GroupLocalService groupService;

    @Reference(unbind = "-")
    private Portal portal;

    @Reference(unbind = "-")
    private FacetedSearcherManager facetedSearcherManager;

    @Reference(unbind = "-")
    private SearchResultProcessorServiceTracker searchResultProcessorServicetracker;

    @Reference(unbind = "-")
    private HitsProcessorRegistry hitsProcessorRegistry;

    @Reference
    private SearchFacetDisplayHandlerServiceTracker searchFacetDisplayHandlerServiceTracker;

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
    public void saveADT(String adtUuid, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        this.storageEngine.saveADT(adtUuid, preferences);
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
    public Map<Group, List<DDMTemplate>> getApplicationDisplayTemplates(PermissionChecker permissionChecker, long groupId) throws PortalException {
        HashMap<Group, List<DDMTemplate>> adts = new HashMap<>();

        long[] currentGroupIds = this.portal.getCurrentAndAncestorSiteGroupIds(groupId);
        long classNameId = this.classNameService.getClassNameId(FlashlightSearchService.ADT_CLASS);
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
        List<String> selectedAssetTypes = tab.getAssetTypes();
        Map<String, String> contentTemplates = tab.getContentTemplates();
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

        AssetEntriesFacet assetEntriesFacet = new AssetEntriesFacet(searchContext);
        searchContext.setEntryClassNames(selectedAssetTypes.toArray(new String[selectedAssetTypes.size()]));
        searchContext.addFacet(assetEntriesFacet);

        DDMStructureFacet structureFacet = new DDMStructureFacet(searchContext);
        String[] ddmStructures = contentTemplates.keySet()
            .stream()
            .map(structureUuid -> {
                String structureKey;
                List<DDMStructure> structures = this.ddmStructureService.getDDMStructuresByUuidAndCompanyId(structureUuid, searchContext.getCompanyId());
                if(structures.size() == 1) {
                    structureKey = structures.get(0).getStructureKey();
                } else {
                    // Ambiguous or unavailable structure
                    structureKey = null;
                }
                return structureKey;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet())
            .toArray(new String[contentTemplates.size()]);

        structureFacet.setValues(ddmStructures);
        searchContext.addFacet(structureFacet);

        addConfiguredFacets(searchContext, tab);

        Hits hits = searcher.search(searchContext);
        this.hitsProcessorRegistry.process(searchContext, hits);

        List<SearchResult> searchResults = new ArrayList<>(end - start);

        for (Document document : hits.getDocs()) {
            String assetType = document.getField(Field.ENTRY_CLASS_NAME).getValue();
            long scopeGroupId = Long.parseLong(document.getField(Field.SCOPE_GROUP_ID).getValue());
            String ddmStructureKey = document.getField(DocumentField.DDM_STRUCTURE_KEY.getName()).getValue();
            SearchResult processedDocument;

            try {
                DDMStructure structure = this.searchStructure(scopeGroupId, assetType, ddmStructureKey);

                if(contentTemplates.containsKey(structure.getUuid())) {
                    processedDocument = this.processDocument(request, response, searchContext, config, tab, assetType, structure, document);
                } else {
                    LOG.debug("Template not defined for given document - skipping");
                    processedDocument = null;
                }
            } catch (SearchResultProcessorException | PortalException e) {
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
     * @param request The portlet request that triggered the search
     * @param response The portlet response that triggered the search
     * @param searchContext The search context
     * @param configuration The search configuration
     * @param tab The tab in which the search is performed
     * @param assetType The document's asset type
     * @param structure The document's DDM structure
     * @param document The document itself
     *
     * @return A search result or null if no processor can handle the document
     *
     * @throws SearchResultProcessorException Thrown if an active processor was unable to process the document
     */
    private SearchResult processDocument(PortletRequest request, PortletResponse response, SearchContext searchContext, FlashlightSearchConfiguration configuration, FlashlightSearchConfigurationTab tab, String assetType, DDMStructure structure, Document document) throws SearchResultProcessorException {
        SearchResultProcessor processor = this.searchResultProcessorServicetracker.getSearchResultProcessor(assetType);
        SearchResult result;

        if(processor != null) {
            result = processor.process(request, response, searchContext, tab, structure, document);
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Search a DDM structure in the given group ID and its ancestors
     *
     * @param groupId The group ID from which to start the search
     * @param assetType The asset type on which the structure must be applied
     * @param structureKey The structure key
     * @return The DDM structure or null if none found or unavailable
     * @throws PortalException If the structure is not found
     */
    private DDMStructure searchStructure(long groupId, String assetType, String structureKey) throws PortalException {
        long classNameId = this.classNameService.getClassNameId(assetType);
        return this.ddmStructureService.getStructure(groupId, classNameId, structureKey, true);
    }


}
