package com.savoirfairelinux.flashlight.service.customizer;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import com.liferay.portal.kernel.search.SearchContext;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;

/**
 * Entry point to customize the search context just before the search is executed.
 */
public interface FlashlightSearchContextCustomizer {
    /**
     * Customize the search context.
     * @param request current portlet request.
     * @param response current portlet response.
     * @param config global flashligh portlet configuration.
     * @param tab current flashlight portlet tab (there is one search launched per tab)
     * @param searchContext the searchcontext to customize.
     */
    void customizeSearchContext(PortletRequest request, PortletResponse response, FlashlightSearchConfiguration config, FlashlightSearchConfigurationTab tab, SearchContext searchContext);
}
