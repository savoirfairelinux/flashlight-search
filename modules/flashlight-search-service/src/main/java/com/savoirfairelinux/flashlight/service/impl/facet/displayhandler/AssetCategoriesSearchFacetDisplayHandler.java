// Copyright 2017 Savoir-faire Linux
// This file is part of Flashlight Search.

// Flashlight Search is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Flashlight Search is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Flashlight Search.  If not, see <http://www.gnu.org/licenses/>.

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
