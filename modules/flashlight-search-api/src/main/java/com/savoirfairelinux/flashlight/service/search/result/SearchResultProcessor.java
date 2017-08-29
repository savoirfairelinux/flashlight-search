package com.savoirfairelinux.flashlight.service.search.result;

import java.util.Collection;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.model.SearchResult;
import com.savoirfairelinux.flashlight.service.search.result.exception.SearchResultProcessorException;

/**
 * <p>
 *   A search result processor transforms a search result document into a Flashlight-specific search result. It is then
 *   consumed by the view.
 * </p>
 * <p>
 *   A search result processor must declare:
 * </p>
 * <ul>
 *   <li>The asset type that it can transform</li>
 *   <li>A set of facets used to isolate its assets</li>
 * </ul>
 */
public interface SearchResultProcessor {

    /**
     * Returns the facets used to obtain search results to be processed by this processor.
     *
     * @param searchContext The search context
     * @param configuration The search portlet configuration
     * @param tab The configuration tab in which the processor is used
     * @return The facets used to obtain search results to be processed by this processor. It never returns null.
     */
    public Collection<Facet> getFacets(SearchContext searchContext, FlashlightSearchConfiguration configuration, FlashlightSearchConfigurationTab tab);

    /**
     * Creates a search result model representing the given search result document
     *
     * @param searchResultDocument The search result document to process
     * @param request The portlet request
     * @param response The portlet response
     * @param searchContext The search context
     * @param configurationTab The search tab in which the search is performed
     * @return The search result model representing the given search result document
     *
     * @throws SearchResultProcessorException If the processor is unable to process the given document
     */
    public SearchResult process(Document searchResultDocument, PortletRequest request, PortletResponse response, SearchContext searchContext, FlashlightSearchConfigurationTab configurationTab) throws SearchResultProcessorException;

    /**
     * @return The asset type supported by the processor
     */
    public String getAssetType();

}
