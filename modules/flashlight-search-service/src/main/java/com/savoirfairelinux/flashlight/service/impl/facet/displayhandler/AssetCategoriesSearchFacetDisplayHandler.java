package com.savoirfairelinux.flashlight.service.impl.facet.displayhandler;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.savoirfairelinux.flashlight.service.facet.SearchFacetDisplayHandler;

@Component(
    service = SearchFacetDisplayHandler.class,
    immediate = true,
    property = {
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=0"
    }
)
public class AssetCategoriesSearchFacetDisplayHandler implements SearchFacetDisplayHandler {

    private static final Log LOG = LogFactoryUtil.getLog(AssetCategoriesSearchFacetDisplayHandler.class);

    @Reference
    private AssetCategoryService assetCategoryService;

    @Override
    public String getSearchFacetClassName() {
        return "com.liferay.portal.search.web.internal.facet.AssetCategoriesSearchFacet";
    }

    @Override
    public String displayTerm(HttpServletRequest request, FacetConfiguration facetConfiguration, String queryTerm) {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        Locale locale = themeDisplay.getLocale();
        try {
            AssetCategory assetCategory = assetCategoryService.getCategory(Long.parseLong(queryTerm));
            return assetCategory.getTitle(locale);
        } catch (PortalException e) {
            LOG.warn("Could not retrieve AssetCategory from id [" + queryTerm + "]", e);
            return queryTerm;
        }
    }
}
