package com.savoirfairelinux.flashlight.service.impl.facet.displayhandler;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.savoirfairelinux.flashlight.service.facet.SearchFacetDisplayHandler;

@Component(
    service = SearchFacetDisplayHandler.class,
    immediate = true,
    property = {
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=0"
    }
)
public class ScopeSearchFacetDisplayHandler implements SearchFacetDisplayHandler {

    private static final Log LOG = LogFactoryUtil.getLog(ScopeSearchFacetDisplayHandler.class);

    @Reference
    private GroupLocalService groupLocalService;



    @Override
    public String getSearchFacetClassName() {
        return "com.liferay.portal.search.web.internal.facet.ScopeSearchFacet";
    }

    @Override
    public String displayTerm(HttpServletRequest request, SearchFacet searchFacet, String queryTerm) {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        Locale locale = themeDisplay.getLocale();
        try {
            Group group = groupLocalService.getGroup(Long.parseLong(queryTerm));
            return group.getDescriptiveName(locale);
        } catch (PortalException e) {
            LOG.warn("Could not retrieve Scope/Group label from id [" + queryTerm + "]", e);
            return queryTerm;
        }
    }
}
