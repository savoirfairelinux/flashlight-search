package com.savoirfairelinux.flashlight.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;

import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.model.SearchResultFacet;
import com.savoirfairelinux.flashlight.service.model.SearchResultsContainer;

public interface FlashlightSearchService {

    public static final Class<FlashlightSearchService> ADT_CLASS = FlashlightSearchService.class;

    /**
     * Reads the configuration stored in portlet preferences and returns a model object corresponding to the
     * configuration. Bear in mind that the configuration cannot be considered valid. For example, the configuration may
     * refer to inexisting database objects as the configuration is not kept in sync with every Liferay object it refers
     * to.
     *
     * @param preferences The portlet preferences
     * @return The configuration model
     *
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public FlashlightSearchConfiguration readConfiguration(PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

    /**
     * Saves the application display template into the configuration
     *
     * @param adtUuid The ADT's UUID
     * @param doSearchOnStartup True to perform a search as soon as the portlet is displayed
     * @param preferences The portlet preferences
     *
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public void saveGlobalSettings(String adtUuid, boolean doSearchOnStartup, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

    /**
     * Writes the given configuration tab model into the configuration. No format validation is performed at this level.
     * Note that at this point, no facet configuration is altered. Only the selected facets themselves are altered.
     *
     * @param configurationTab The configuration tab model
     * @param preferences The portlet preferences
     *
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public void saveConfigurationTab(FlashlightSearchConfigurationTab configurationTab, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

    /**
     * Writes the given facet configuration into the tab's configuration. No format validation is performed at this
     * level.
     *
     * @param configurationTab The tab for which we configure facets
     * @param searchFacet The facet to save
     * @param preferences The portlet preferences
     *
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public void saveSearchFacetConfig(FlashlightSearchConfigurationTab configurationTab, SearchFacet searchFacet, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

    /**
     * Removes the given configuration tab from the configuration
     *
     * @param tabId The tab's unique ID
     * @param preferences The portlet preferences
     *
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public void deleteConfigurationTab(String tabId, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

    /**
     * Returns the list of DDM structures that are visible to the given site and its parents
     *
     * @param groupId The site from which to start the search for DDM templates
     * @return A list of DDM structures, accessible from the given context. They are indexed by DDM structure class names.
     * @throws PortalException If an error occurs while fetching the structures
     */
    public Map<String, List<DDMStructure>> getDDMStructures(long groupId) throws PortalException;

    /**
     * Returns the list of file entry type templates that are visible to the given site and its parents
     *
     * @param groupId The site from which to start the search for file entry templates
     * @return A list of file entry templates, accessible from the given context. They are indexed by DLFileEntryType.
     * @throws PortalException If an error occurs while fetching the data
     */
    public Map<DLFileEntryType, List<DDMTemplate>> getFileEntryTypes(PermissionChecker permissionChecker, long groupId) throws PortalException;

    /**
     * Returns the list of application display templates that can be used with the Flashlight search portlet
     *
     * @param permissionChecker The current context's permission checker
     * @param groupId The current site ID
     * @return The application display templates, indexed by site
     *
     * @throws PortalException If an error occurs while fetching the templates
     */
    public Map<Group, List<DDMTemplate>> getApplicationDisplayTemplates(PermissionChecker permissionChecker, long groupId) throws PortalException;

    /**
     * @return A list of asset types that the search engine supports
     */
    public List<String> getSupportedAssetTypes();

    /**
     * @return The list of search facets supported by the application
     */
    public List<SearchFacet> getSupportedSearchFacets();

    /**
     * Format a facet term in the search result.
     *
     * @see com.savoirfairelinux.flashlight.service.facet.SearchFacetDisplayHandler
     *
     * @param request the current request.
     * @param searchResultFacet the configured and initialized search facet.
     * @param queryTerm the current term to format.
     * @return a user-displayable term, or the raw queryTerm if no formatting is applicable.
     */
    public String displayTerm(HttpServletRequest request, SearchResultFacet searchResultFacet, String queryTerm);

    /**
     * Performs a search
     *
     * @param request The request that trigerred the search
     * @param response The response that triggered the search
     * @param tabId The search tab's ID. May be left null.
     * @param pageOffset The number of pages from which to start (start at 0)
     * @param isLoadMore True if the given search is to get more results out of a previous search in the given tab
     *
     * @return The search results
     *
     * @throws ReadOnlyException If the portlet configuration is read-only
     * @throws ValidatorException If the portlet configuration is invalid
     * @throws IOException If the portlet configuration cannot be read
     * @throws SearchException If the search fails
     */
    public SearchResultsContainer search(PortletRequest request, PortletResponse response, String tabId, int pageOffset, boolean isLoadMore) throws ReadOnlyException, ValidatorException, IOException, SearchException;

}
