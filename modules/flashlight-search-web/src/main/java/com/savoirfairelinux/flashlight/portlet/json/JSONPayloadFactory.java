package com.savoirfairelinux.flashlight.portlet.json;

import java.util.List;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringPool;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.model.SearchPage;
import com.savoirfairelinux.flashlight.service.model.SearchResult;

/**
 * This factory is used to create search results JSON payloads
 */
public class JSONPayloadFactory {

    private static final String FIELD_RESULTS = "results";
    private static final String FIELD_LOAD_MORE_URL = "loadMoreUrl";
    private static final String FIELD_RESULT_TITLE = "title";
    private static final String FIELD_RESULT_VIEW_URL = "url";
    private static final String FIELD_RESULT_RENDERING = "html";

    private JSONFactory jsonFactory;

    /**
     * Creates the factory
     * @param jsonFactory The JSON factory
     */
    public JSONPayloadFactory(JSONFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    /**
     * Creates a search result JSON payload
     *
     * @param response The resource response currently in effect
     * @param tab The search tab
     * @param page The tab's search page
     * @param givenOffset The search offset that was used during the search
     * @param loadMoreUrl The URL used to called "load more"
     * @return The search result JSON payload
     */
    public JSONObject createJSONPayload(FlashlightSearchConfigurationTab tab, SearchPage page, int givenOffset, String loadMoreUrl) {
        JSONObject json = this.jsonFactory.createJSONObject();
        JSONArray jsonResults = this.jsonFactory.createJSONArray();
        List<SearchResult> results = page.getSearchResults();

        for(SearchResult result : results) {
            JSONObject jsonResult = this.jsonFactory.createJSONObject();
            jsonResult.put(FIELD_RESULT_TITLE, result.getTitle());
            jsonResult.put(FIELD_RESULT_VIEW_URL, result.getViewUrl());
            jsonResult.put(FIELD_RESULT_RENDERING, result.getRendering());
            jsonResults.put(jsonResult);
        }

        json.put(FIELD_RESULTS, jsonResults);

        int pageSize = tab.getFullPageSize();
        int loadMoreSize = tab.getLoadMorePageSize();

        // If we would have more results after the next offset, put the URL. Otherwize, put nothing.
        String jsonLoadMoreUrl;
        if(pageSize + (loadMoreSize * (givenOffset + 1)) <= page.getTotalSearchResults()) {
            jsonLoadMoreUrl = loadMoreUrl;
        } else {
            jsonLoadMoreUrl = StringPool.BLANK;
        }

        json.put(FIELD_LOAD_MORE_URL, jsonLoadMoreUrl);

        return json;
    }

}
