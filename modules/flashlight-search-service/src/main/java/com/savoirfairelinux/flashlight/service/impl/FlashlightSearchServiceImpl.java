package com.savoirfairelinux.flashlight.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.dynamic.data.mapping.util.DDMTemplatePermissionSupport;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorage;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorageV1;
import com.savoirfairelinux.flashlight.service.impl.search.result.SearchResultProcessorServiceTracker;
import com.savoirfairelinux.flashlight.service.model.SearchPage;
import com.savoirfairelinux.flashlight.service.model.SearchResult;
import com.savoirfairelinux.flashlight.service.model.SearchResultsContainer;
import com.savoirfairelinux.flashlight.service.search.result.SearchResultProcessor;
import com.savoirfairelinux.flashlight.service.search.result.exception.SearchResultProcessorException;

@Component(
    service = FlashlightSearchService.class,
    immediate = true
)
public class FlashlightSearchServiceImpl implements FlashlightSearchService {

    private static final Log LOG = LogFactoryUtil.getLog(FlashlightSearchServiceImpl.class);

    @Reference(unbind = "-")
    private ClassNameLocalService classNameService;

    @Reference(unbind = "-")
    private DDMStructureLocalService ddmStructureService;

    @Reference(unbind = "-")
    private DDMTemplateLocalService ddmTemplateService;

    @Reference(unbind = "-")
    private DDMTemplatePermissionSupport ddmTemplatePermissionSupport;

    @Reference(unbind = "-")
    private GroupLocalService groupService;

    @Reference(unbind = "-")
    private Portal portal;

    @Reference(unbind = "-")
    private FacetedSearcherManager facetedSearcherManager;

    @Reference(unbind = "-")
    private SearchResultProcessorServiceTracker searchResultProcessorServicetracker;

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
                    String modelResourceName = this.ddmTemplatePermissionSupport.getResourceName(template.getResourceClassNameId());
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
    public SearchResultsContainer search(PortletRequest request, PortletResponse response) throws ReadOnlyException, ValidatorException, IOException, SearchException {
        FlashlightSearchConfiguration config = this.readConfiguration(request.getPreferences());
        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        FacetedSearcher searcher = this.facetedSearcherManager.createFacetedSearcher();

        LinkedHashMap<FlashlightSearchConfigurationTab, SearchPage> resultMap = new LinkedHashMap<>(tabs.size());
        for(FlashlightSearchConfigurationTab tab : tabs.values()) {
            resultMap.put(tab, this.search(request, response, config, tab, searcher));
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
     * @return A search page
     *
     * @throws SearchException If an error occurs during search
     */
    private SearchPage search(PortletRequest request, PortletResponse response, FlashlightSearchConfiguration config, FlashlightSearchConfigurationTab tab, FacetedSearcher searcher) throws SearchException {
        List<String> selectedAssetTypes = tab.getAssetTypes();
        SearchContext searchContext = SearchContextFactory.getInstance(this.portal.getHttpServletRequest(request));

        // Configure asset types and search
        List<Facet> searchFacets = SearchFacetTracker.getSearchFacets()
            .stream()
            .map(f -> {
                Facet initializedFacet;
                try {
                    f.init(searchContext.getCompanyId(), StringPool.BLANK);
                    initializedFacet = f.getFacet();
                } catch(Exception e) {
                    LOG.error("Cannot initialize search facet", e);
                    initializedFacet = null;
                }
                return initializedFacet;
            })
            .filter(f -> f != null)
            .collect(Collectors.toList());
        searchContext.setFacets(searchFacets);
        searchContext.setEntryClassNames(selectedAssetTypes.toArray(new String[selectedAssetTypes.size()]));
        searchContext.setStart(0);
        searchContext.setEnd(tab.getPageSize());

        Hits hits = searcher.search(searchContext);
        List<Document> searchableDocuments = Arrays.asList(hits.getDocs())
            .stream()
            .filter(doc -> this.isSearchable(tab, doc))
            .collect(Collectors.toList());
        List<SearchResult> searchResults = new ArrayList<>(searchableDocuments.size());

        for (Document document : searchableDocuments) {
            String assetType = document.getField(Field.ENTRY_CLASS_NAME).getValue();
            long scopeGroupId = Long.parseLong(document.getField(Field.SCOPE_GROUP_ID).getValue());
            String ddmStructureKey = document.getField(DocumentField.DDM_STRUCTURE_KEY.getName()).getValue();
            SearchResult processedDocument;

            try {
                DDMStructure structure = this.searchStructure(scopeGroupId, assetType, ddmStructureKey);

                if(tab.getContentTemplates().containsKey(structure.getUuid())) {
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

        return new SearchPage(searchResults, hits.getLength());
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
     * Indicates is the given document is searchable in the given tab context
     * @param tab The tab context
     * @param document The document
     *
     * @return <p>True if :</p>
     *         <ul>
     *           <li>The document is of <strong>an asset type supported by the given tab</strong></li>
     *           <li>The document has a <strong>non-empty structure key field</strong></li>
     *         </ul>
     */
    private boolean isSearchable(FlashlightSearchConfigurationTab tab, Document document) {
        boolean searchable = true;
        String assetType = document.getField(Field.ENTRY_CLASS_NAME).getValue();
        Field ddmStructureKeyField = document.getField(DocumentField.DDM_STRUCTURE_KEY.getName());

        if(!tab.getAssetTypes().contains(assetType)) {
            searchable = false;
            LOG.debug("Omitting search document - asset type not configured");
        }

        if(searchable && (ddmStructureKeyField == null || ddmStructureKeyField.getValue().isEmpty())) {
            searchable = false;
            LOG.debug("Omitting search document - structure not found");
        }

        return searchable;
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
