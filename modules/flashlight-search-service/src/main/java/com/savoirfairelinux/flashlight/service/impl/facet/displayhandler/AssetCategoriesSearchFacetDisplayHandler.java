package com.savoirfairelinux.flashlight.service.impl.facet.displayhandler;

import java.util.Locale;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.web.facet.SearchFacet;
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
    public String displayTerm(Locale locale, SearchFacet searchFacet, String queryTerm) {
        try {
            AssetCategory assetCategory = assetCategoryService.getCategory(Long.parseLong(queryTerm));
            return assetCategory.getTitle(locale);
        } catch (PortalException e) {
            LOG.warn("Could not retrieve AssetCategory from id [" + queryTerm + "]", e);
            return queryTerm;
        }
    }
}
