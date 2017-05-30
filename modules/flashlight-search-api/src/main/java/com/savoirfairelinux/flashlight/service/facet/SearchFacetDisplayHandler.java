package com.savoirfairelinux.flashlight.service.facet;


import javax.servlet.http.HttpServletRequest;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;

public interface SearchFacetDisplayHandler {
    String getSearchFacetClassName();

    String displayTerm(HttpServletRequest request, FacetConfiguration facetConfiguration, String queryTerm);
}
