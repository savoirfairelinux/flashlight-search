package com.savoirfairelinux.flashlight.service.impl.facet.displayhandler;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.*;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.StringPool;
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
public class FolderSearchFacetDisplayHandler implements SearchFacetDisplayHandler {

    private static final Log LOG = LogFactoryUtil.getLog(FolderSearchFacetDisplayHandler.class);

    @Reference
    private Language language;

    @Override
    public String getSearchFacetClassName() {
        return "com.liferay.portal.search.web.internal.facet.FolderSearchFacet";
    }

    @Override
    public String displayTerm(HttpServletRequest request, SearchFacet searchFacet, String queryTerm) {
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
