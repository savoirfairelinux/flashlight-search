package com.savoirfairelinux.flashlight.service.facet;


import javax.servlet.http.HttpServletRequest;
import com.liferay.portal.search.web.facet.SearchFacet;

public interface SearchFacetDisplayHandler {
    String getSearchFacetClassName();

    String displayTerm(HttpServletRequest request, SearchFacet searchFacet, String queryTerm);
}
