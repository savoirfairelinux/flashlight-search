package com.savoirfairelinux.flashlight.service.search.result;

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
 * A search result processor transforms a search result document into a Flashlight-specific search result. It can then
 * be consumed by the view.
 */
public interface SearchResultProcessor {

    public Facet getFacet(SearchContext searchContext, FlashlightSearchConfiguration configuration, FlashlightSearchConfigurationTab tab);

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
