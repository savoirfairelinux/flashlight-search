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
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.*;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.savoirfairelinux.flashlight.service.facet.SearchFacetDisplayHandler;

@Component(
    service = SearchFacetDisplayHandler.class,
    immediate = true,
    property = {
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=0"
    }
)
public class FolderSearchFacetDisplayHandler implements SearchFacetDisplayHandler {

    private static final Log LOG = LogFactoryUtil.getLog(FolderSearchFacetDisplayHandler.class);

    @Reference
    private Language language;

    @Override
    public String getSearchFacetClassName() {
        return "com.liferay.portal.search.web.internal.facet.FolderSearchFacet";
    }

    @Override
    public String displayTerm(HttpServletRequest request, FacetConfiguration facetConfiguration, String queryTerm) {
        String folderName = queryTerm;
        if ("0".equals(queryTerm)) {
            ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
            Locale locale = themeDisplay.getLocale();
            folderName = language.get(locale, "root-folder");
        } else {
            Indexer<?> indexer = FolderSearcher.getInstance();
            SearchContext searchContext = SearchContextFactory.getInstance(request);
            searchContext.setFolderIds(new long[]{Long.parseLong(queryTerm)});
            searchContext.setKeywords(StringPool.BLANK);
            try {
                Hits results = indexer.search(searchContext);
                if (results.getLength() > 0) {
                    Document document = results.doc(0);
                    Field title = document.getField(Field.TITLE);
                    folderName = title.getValue();
                }
            } catch (SearchException e) {
                LOG.warn("Could not retrieve folder name from id [" + queryTerm + "]", e);
            }
        }
        return folderName;
    }
}
