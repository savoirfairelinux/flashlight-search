package com.savoirfairelinux.flashlight.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
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
     * @param preferences The portlet preferences
     *
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public void saveADT(String adtUuid, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

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

    public SearchResultsContainer search(PortletRequest request, PortletResponse response) throws ReadOnlyException, ValidatorException, IOException, SearchException;

    public SearchResultsContainer search(PortletRequest request, PortletResponse response, String tabId) throws ReadOnlyException, ValidatorException, IOException, SearchException;

}
