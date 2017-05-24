package com.savoirfairelinux.flashlight.service.facet;


import java.util.Locale;
import com.liferay.portal.search.web.facet.SearchFacet;

public interface SearchFacetDisplayHandler {
    String getSearchFacetClassName();

    String displayTerm(Locale locale, SearchFacet searchFacet, String queryTerm);
}
