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

package com.savoirfairelinux.flashlight.service.impl.configuration;

import java.io.IOException;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import com.liferay.portal.search.web.facet.SearchFacet;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;

/**
 * The configuration storage interface is used to read and write application configuration in a versionned manner
 */
public interface ConfigurationStorage {

    /**
     * Key in the portlet preferences that holds the version of the configuration
     */
    public static final String PORTLET_PREFERENCES_VERSION_KEY = "configuration-version";

    /**
     * Version to put in the portlet preferences if none are found
     */
    public static final String PORTLET_PREFERENCES_DEFAULT_VERSION = "1";

    /**
     * Reads the configuration stored in portlet preferences and returns a model object corresponding to the
     * configuration. Bear in mind that the configuration cannot be considered valid. For example, the configuration may
     * refer to inexisting database objects as the configuration is not kept in sync with every Liferay object it refers
     * to.
     *
     * @param preferences The portlet preferences
     * @return The configuration model
     */
    public FlashlightSearchConfiguration readConfiguration(PortletPreferences preferences);

    /**
     * Saves the application's global settings
     *
     * @param adtUuid The ADT's UUID
     * @param doSearchOnStartup True to perform a search as soon as the portlet is displayed
     * @param doSearchOnStartupKeywords The keywords to send to the search if a search is triggered on startup
     * @param preferences The portlet preferences
     *
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public void saveGlobalSettings(String adtUuid, boolean doSearchOnStartup, String doSearchOnStartupKeywords, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

    /**
     * Writes the given configuration tab model into the configuration. No format validation is performed at this level.
     * It is the developer's responsibility to send data that is in the expected format.
     *
     * Also, this method does not alter facet configurations. It only alters which facets are selected.
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
     * Delete the given configuration tab
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

}
