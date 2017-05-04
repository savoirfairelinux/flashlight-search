package com.savoirfairelinux.flashlight.service;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.search.web.facet.SearchFacet;

public interface SearchService {

    public static final String PORTLET_NAME = "com_savoirfairelinux_flashlight_portlet_FlashlightSearchPortlet";

    public Map<String, List<Document>> customGroupedSearch(SearchContext searchContext, PortletPreferences preferences, String groupBy, int maxCount);

    public List<SearchFacet> getEnabledSearchFacets(PortletPreferences preferences);

}
