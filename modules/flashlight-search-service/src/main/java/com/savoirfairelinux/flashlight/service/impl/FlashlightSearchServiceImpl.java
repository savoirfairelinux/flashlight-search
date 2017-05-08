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

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.dynamic.data.mapping.util.DDMTemplatePermissionSupport;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.ScopeFacet;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorage;
import com.savoirfairelinux.flashlight.service.impl.configuration.ConfigurationStorageV1;

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
    private JournalArticleLocalService journalArticleService;
    private Portal portal;
    private FacetedSearcherManager facetedSearcherManager;

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
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap(),
                StringPool.BLANK
            );
        }

        return returnedConfig;
    }

    @Override
    public void writeConfiguration(FlashlightSearchConfiguration configuration, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException {
        this.storageEngines[this.latestConfigurationVersion - 1].writeConfiguration(configuration, preferences);
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
    public Map<String, List<Document>> customGroupedSearch(SearchContext searchContext, PortletPreferences portletPreferences, String groupBy, int maxCount) throws ReadOnlyException, ValidatorException, IOException, SearchException {
        FlashlightSearchConfiguration config = this.readConfiguration(portletPreferences);
        Hits hits = this.customSearch(searchContext, config);
        Map<String, List<Document>> results;

        if (hits != null) {

            Document[] documents = hits.getDocs();
            results = new HashMap<>();

            for (Document document : documents) {
                String key = groupBy;
                if (document.get(Field.ENTRY_CLASS_NAME).equals(JournalArticle.class.getName())) {
                    key = "ddmStructureKey";
                    JournalArticle journalArticle = this.journalArticleService.fetchArticle(GetterUtil.getLong(document.get("groupId")), document.get("articleId"));
                    String templateKey = portletPreferences.getValue("ddm-"+document.get("ddmStructureKey"), document.get("ddmTemplateKey"));
                    try {
                        String content  = this.journalArticleService.getArticleContent(journalArticle, templateKey, Constants.VIEW, searchContext.getLanguageId(), null, null);
                        document.addKeyword("journalContent", content);
                    } catch (PortalException e) {
                        LOG.error("Cannot retrieve article content",e);
                    }
                } else if (document.get(Field.ENTRY_CLASS_NAME).equals(DLFileEntry.class.getName())) {
                    key = "fileEntryTypeId";
                }

                document.addKeyword("type", key);
                String keyValue = document.get(key);
                if (!results.containsKey(keyValue)) {
                    List<Document> list = new ArrayList<Document>();
                    list.add(document);
                    results.put(keyValue, list);
                } else if (results.get(keyValue).size() < maxCount) {
                    results.get(keyValue).add(document);
                }
            }
        } else {
            results = Collections.emptyMap();
        }

        return results;
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

    private Hits customSearch(SearchContext searchContext, FlashlightSearchConfiguration config) throws SearchException {
        // Adding the facets
        Facet assetEntriesFacet = new AssetEntriesFacet(searchContext);
        assetEntriesFacet.setStatic(true);
        searchContext.addFacet(assetEntriesFacet);

        Facet scopeFacet = new ScopeFacet(searchContext);
        scopeFacet.setStatic(true);
        searchContext.addFacet(scopeFacet);

        List<String> selectedFacetNames = config.getSelectedFacets();
        List<SearchFacet> enabledFacets = SearchFacetTracker.getSearchFacets()
            .stream()
            .filter(facet -> selectedFacetNames.contains(facet.getClassName()))
            .collect(Collectors.toList());

        for (SearchFacet searchFacet : enabledFacets) {
            try {
                searchFacet.init(searchContext.getCompanyId(), StringPool.BLANK, searchContext);
            } catch (Exception e) {
                LOG.warn("cannot init the facet " + searchFacet.getClass().getName(), e);
            }
            Facet facet = searchFacet.getFacet();
            if (facet != null) {
                searchContext.addFacet(facet);
            }
        }

         // Actual search
        List<String> selectedAssetTypes = config.getSelectedAssetTypes();
        if (searchContext.getEntryClassNames().length > 1) {
            searchContext.setEntryClassNames(selectedAssetTypes.toArray(new String[selectedAssetTypes.size()]));
        }

        FacetedSearcher facetedSearcher = this.facetedSearcherManager.createFacetedSearcher();
        return facetedSearcher.search(searchContext);
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
    public void setJournalArticleService(JournalArticleLocalService service) {
        this.journalArticleService = service;
    }

    @Reference(unbind = "-")
    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    @Reference(unbind = "-")
    public void setFacetedSearcherManager(FacetedSearcherManager facetedSearcherManager) {
        this.facetedSearcherManager = facetedSearcherManager;
    }

}
