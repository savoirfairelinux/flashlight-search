package com.savoirfairelinux.flashlight.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.portlet.PortletPreferences;
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
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorage;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorageV1;
import com.savoirfairelinux.flashlight.service.impl.search.result.SearchResultProcessorServiceTracker;
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

    private ClassNameLocalService classNameService;
    private DDMStructureLocalService ddmStructureService;
    private DDMTemplateLocalService ddmTemplateService;
    private DDMTemplatePermissionSupport ddmTemplatePermissionSupport;
    private GroupLocalService groupService;
    private Portal portal;
    private FacetedSearcherManager facetedSearcherManager;
    private SearchResultProcessorServiceTracker searchResultProcessorServicetracker;

    private ConfigurationStorage[] storageEngines;
    private int latestConfigurationVersion;

    @Activate
    protected void activate() {
        this.storageEngines = new ConfigurationStorage[1];
        this.storageEngines[0] = new ConfigurationStorageV1();
        this.latestConfigurationVersion = this.storageEngines.length;
    }

    @Override
    public FlashlightSearchConfiguration readConfiguration(PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        FlashlightSearchConfiguration returnedConfig;
        int configVersion = Integer.parseInt(preferences.getValue(ConfigurationStorage.PORTLET_PREFERENCES_VERSION_KEY, ConfigurationStorage.PORTLET_PREFERENCES_DEFAULT_VERSION));
        boolean failedMigration = false;

        if(configVersion != this.latestConfigurationVersion) {
            try {
                this.migrateConfiguration(preferences, configVersion);
            } catch(ReadOnlyException | ValidatorException | IOException e) {
                LOG.error("Error during configuration migration", e);
                failedMigration = true;
            }
        }

        if(!failedMigration) {
            returnedConfig = this.storageEngines[configVersion - 1].readConfiguration(preferences);
        } else {
            LOG.warn("Due to a failure in configuration migration, an empty configuration has been returned");
            returnedConfig = new FlashlightSearchConfiguration(
                StringPool.BLANK,
                Collections.emptyList()
            );
        }

        return returnedConfig;
    }

    @Override
    public void saveADT(String adtUuid, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        this.getLatestStorageEngine().saveADT(adtUuid, preferences);
    }

    @Override
    public void saveConfigurationTab(FlashlightSearchConfigurationTab configurationTab, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        this.getLatestStorageEngine().saveConfigurationTab(configurationTab, preferences);
    }

    @Override
    public void deleteConfigurationTab(String tabId, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        this.getLatestStorageEngine().deleteConfigurationTab(tabId, preferences);
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
    public SearchResultsContainer search(SearchContext searchContext, PortletPreferences portletPreferences, int maxCount) throws ReadOnlyException, ValidatorException, IOException, SearchException {
        FlashlightSearchConfiguration config = this.readConfiguration(portletPreferences);
        // FIXME TEMP
        List<String> selectedAssetTypes = Collections.emptyList();

        // Configure asset types and search
        searchContext.setEntryClassNames(selectedAssetTypes.toArray(new String[selectedAssetTypes.size()]));
        FacetedSearcher facetedSearcher = this.facetedSearcherManager.createFacetedSearcher();
        Hits hits = facetedSearcher.search(searchContext);

        Map<DDMStructure, List<SearchResult>> searchResultsIndex;
        Map<String, DDMStructure> structureIndex = new HashMap<>();

        if (hits != null) {
            Document[] documents = hits.getDocs();
            searchResultsIndex = new HashMap<>();

            for (Document document : documents) {
                String assetType = document.getField(Field.ENTRY_CLASS_NAME).getValue();
                long scopeGroupId = Long.parseLong(document.getField(Field.SCOPE_GROUP_ID).getValue());
                Field ddmStructureKeyField = document.getField(DocumentField.DDM_STRUCTURE_KEY.getName());

                if(ddmStructureKeyField != null) {
                    String ddmStructureKey = ddmStructureKeyField.getValue();
                    DDMStructure structure = this.searchStructure(scopeGroupId, assetType, ddmStructureKey);
                    if(structure != null) {
                        structureIndex.put(structure.getUuid(), structure);
                        List<SearchResult> searchResults;
                        if(searchResultsIndex.containsKey(structure)) {
                            searchResults = searchResultsIndex.get(structure);
                        } else {
                            searchResults = new ArrayList<>();
                            searchResultsIndex.put(structure, searchResults);
                        }
                        searchResults.add(this.processDocument(searchContext, config, assetType, structure, document));
                    } else {
                        LOG.debug("No DDM structure found within scope group ID " + scopeGroupId + " and structure key \"" + ddmStructureKey + "\"");
                    }
                } else {
                    LOG.debug("No DDM structure key for document UID \"" + document.getUID() + "\", ignoring");
                }



            }
        } else {
            searchResultsIndex = Collections.emptyMap();
        }

        return new SearchResultsContainer(searchResultsIndex);
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

    private SearchResult processDocument(SearchContext searchContext, FlashlightSearchConfiguration configuration, String assetType, DDMStructure structure, Document document) {
        SearchResult result = null;

        SearchResultProcessor processor = this.searchResultProcessorServicetracker.getSearchResultProcessor(assetType);
        if(processor != null) {
            try {
                // FIXME TEMP
                result = processor.process(searchContext, configuration.getTabs().get(0), structure, document);
            } catch(SearchResultProcessorException e) {
                LOG.error("Error while processing search result. Not integrating in search results.", e);
            }
        } else {
            LOG.warn("No search result processor registered for asset type \"" + assetType + "\"");
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
     */
    private DDMStructure searchStructure(long groupId, String assetType, String structureKey) {
        DDMStructure foundStructure;
        long classNameId = this.classNameService.getClassNameId(assetType);

        try{
            foundStructure = this.ddmStructureService.getStructure(groupId, classNameId, structureKey, true);
        } catch(PortalException e) {
            foundStructure = null;
            LOG.error("DDM structure not found", e);
        }

        return foundStructure;
    }

    /**
     * Migrates the configuration to the newest possible format
     *
     * @param preferences The portlet preferences
     * @param storedVersion The currently stored version
     *
     * @throws IOException If an error occurs during migration
     * @throws ValidatorException If the configuration is invalid
     * @throws ReadOnlyException If the configuration is read-only
     */
    private void migrateConfiguration(PortletPreferences preferences, int storedVersion) throws IOException, ValidatorException, ReadOnlyException {
        if(this.latestConfigurationVersion > 0 && this.latestConfigurationVersion <= this.storageEngines.length) {
            // Migrate config up to the current version
            while(storedVersion < this.latestConfigurationVersion) {
                this.storageEngines[storedVersion - 1].migrateConfiguration(preferences);
                storedVersion++;
            }
        } else {
            // Something is wrong with the configuration. It cannot be migrated. This should never happen if the
            // configuration is not tampered with. If that happens, return an empty configuration.
            throw new IOException("Cannot migrate the configuration as there is no migration path possible with the current code.");
        }
    }

    /**
     * @return The latest configuration storage engine
     */
    private ConfigurationStorage getLatestStorageEngine() {
        return this.storageEngines[this.latestConfigurationVersion - 1];
    }

    @Reference(unbind = "-")
    public void setClassNameService(ClassNameLocalService classNameService) {
        this.classNameService = classNameService;
    }

    @Reference(unbind = "-")
    public void setDdmStructureService(DDMStructureLocalService ddmStructureService) {
        this.ddmStructureService = ddmStructureService;
    }

    @Reference(unbind = "-")
    public void setDdmTemplatePermissionSupport(DDMTemplatePermissionSupport ddmTemplatePermissionSupport) {
        this.ddmTemplatePermissionSupport = ddmTemplatePermissionSupport;
    }

    @Reference(unbind = "-")
    public void setDdmTemplateService(DDMTemplateLocalService ddmTemplateService) {
        this.ddmTemplateService = ddmTemplateService;
    }

    @Reference(unbind = "-")
    public void setGroupService(GroupLocalService groupService) {
        this.groupService = groupService;
    }

    @Reference(unbind = "-")
    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    @Reference(unbind = "-")
    public void setFacetedSearcherManager(FacetedSearcherManager facetedSearcherManager) {
        this.facetedSearcherManager = facetedSearcherManager;
    }

    @Reference(unbind = "-")
    public void setSearchResultProcessorServiceTracker(SearchResultProcessorServiceTracker searchResultProcessorServiceTracker) {
        this.searchResultProcessorServicetracker = searchResultProcessorServiceTracker;
    }

}
