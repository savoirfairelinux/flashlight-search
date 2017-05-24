package com.savoirfairelinux.flashlight.service.impl.facet.displayhandler;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.savoirfairelinux.flashlight.service.facet.SearchFacetDisplayHandler;

@Component(
    service = SearchFacetDisplayHandler.class,
    immediate = true,
    property = {
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=0"
    }
)
public class ModifiedSearchFacetDisplayHandler implements SearchFacetDisplayHandler {

    @Reference
    private Language language;

    @Override
    public String getSearchFacetClassName() {
        return "com.liferay.portal.search.web.internal.facet.ModifiedSearchFacet";
    }

    @Override
    public String displayTerm(Locale locale, SearchFacet searchFacet, String queryTerm) {
        JSONArray ranges = searchFacet.getFacetConfiguration().getData().getJSONArray("ranges");
        AtomicInteger i = new AtomicInteger(0);
        String rangeLabel = Stream.generate(() -> ranges.getJSONObject(i.getAndIncrement()))
            .limit(ranges.length())
            .filter((JSONObject range) -> queryTerm.equals(range.getString("range")))
            .map((JSONObject range) -> range.getString("label"))
            .findFirst()
            .orElse(queryTerm);
        return language.get(locale, rangeLabel);
    }
}
