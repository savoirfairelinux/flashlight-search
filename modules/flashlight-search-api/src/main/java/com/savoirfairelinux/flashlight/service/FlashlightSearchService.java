package com.savoirfairelinux.flashlight.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.savoirfairelinux.flashlight.portlet.configuration.FlashlightConfiguration;

public interface FlashlightSearchService {

    public static final Class<?>[] SUPPORTED_ASSET_TYPES = {JournalArticle.class, DLFileEntry.class};

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
    public FlashlightConfiguration readConfiguration(PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

    /**
     * Writes the given configuration model into the configuration. No format validation is performed at this level.
     *
     * @param configuration The configuration model
     * @param preferences The portlet preferences
     *
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public void writeConfiguration(FlashlightConfiguration configuration, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

    /**
     * @param groupId The site from which to start the search for DDM templates
     * @return A list of DDM structures, accessible from the given context. They are indexed by DDM structure class names.
     * @throws PortalException If an error occurs while fetching the structures
     */
    public Map<String, List<DDMStructure>> getDDMStructures(long groupId) throws PortalException;

    public Map<String, List<Document>> customGroupedSearch(SearchContext searchContext, PortletPreferences preferences, String groupBy, int maxCount) throws ReadOnlyException, ValidatorException, IOException, SearchException;

}
