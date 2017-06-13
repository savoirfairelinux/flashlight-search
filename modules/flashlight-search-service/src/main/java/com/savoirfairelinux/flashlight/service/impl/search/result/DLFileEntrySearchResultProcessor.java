package com.savoirfairelinux.flashlight.service.impl.search.result;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.model.SearchResult;
import com.savoirfairelinux.flashlight.service.search.result.SearchResultProcessor;
import com.savoirfairelinux.flashlight.service.search.result.exception.SearchResultProcessorException;

/**
 * This processor is used to display file entries
 */
@Component(
    service = SearchResultProcessor.class,
    immediate = true,
    property = {
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=0"
    }
)
public class DLFileEntrySearchResultProcessor implements SearchResultProcessor {

    private static final String ASSET_TYPE = DLFileEntry.class.getName();

    @Override
    public Facet getFacet(SearchContext searchContext, FlashlightSearchConfiguration configuration, FlashlightSearchConfigurationTab tab) {
        return null;
    }

    @Override
    public SearchResult process(PortletRequest request, PortletResponse response, SearchContext searchContext, FlashlightSearchConfigurationTab configurationTab, Document searchResultDocument) throws SearchResultProcessorException {
        return new SearchResult("Rendering!", "#", "Yay!");
    }

    @Override
    public String getAssetType() {
        return ASSET_TYPE;
    }

}
