package com.savoirfairelinux.flashlight.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.liferay.portal.kernel.model.Layout;

/**
 * Container listing all flashlight search portlets found on the current site (same groupId + same public/private visiblity).
 * Is injected as com.savoirfairelinux.flashlight.action.SearchUrlAction#REQUEST_ATTR_FLASHLIGHT_URLS request attribute.
 */
public class SearchUrlContainer {
    private final Map<Layout, List<SearchUrl>> searchUrls;

    SearchUrlContainer(Map<Layout, List<SearchUrl>> searchUrls) {
        this.searchUrls = searchUrls;
    }

    /**
     * Map of all instances of flashlight search portlet, grouped by their pages.
     * @return a map of flashlight search portlet informations, with their current page/Layout as key.
     */
    public Map<Layout, List<SearchUrl>> getSearchUrls() {
        return searchUrls;
    }

    /**
     * Get the list of flashlight search portlets for a specific layout.
     * @param layoutUuid the layout to search flashlight portlet(s) for.
     * @return the list of flashlight search portlets instances found on the page, or an empty list else.
     */
    public List<SearchUrl> getSearchUrlByLayoutUuid(String layoutUuid) {
        return this.searchUrls.entrySet().stream()
            .filter(entry -> entry.getKey().getUuid().equals(layoutUuid))
            .findFirst()
                .map(Map.Entry::getValue)
            .orElse(Collections.emptyList());
    }
}
