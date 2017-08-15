package com.savoirfairelinux.flashlight.service.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.liferay.portal.kernel.model.Layout;

/**
 * Container listing all flashlight search URLs found on the current layout set (current site and layouts with the same
 * public/private visiblity).
 *
 * Is injected as {@link SearchUrlAction#REQUEST_ATTR_FLASHLIGHT_URLS} request attribute.
 */
public class SearchUrlContainer {
    private final Map<Layout, List<SearchUrl>> searchUrls;

    public SearchUrlContainer(Map<Layout, List<SearchUrl>> searchUrls) {
        this.searchUrls = searchUrls;
    }

    /**
     * Map of all instances of flashlight search portlet URLs, grouped by their pages.
     * @return A map of flashlight search portlet URLs, with their current page/Layout as key.
     */
    public Map<Layout, List<SearchUrl>> getSearchUrls() {
        return searchUrls;
    }

    /**
     * Get the list of flashlight search portlet URLs for a specific layout.
     * @param layoutUuid The layout to search flashlight portlet URLs for.
     * @return The list of Flashlight search portlet URLs on the given page, an empty list is none are found.
     */
    public List<SearchUrl> getSearchUrlByLayoutUuid(String layoutUuid) {
        return this.searchUrls.entrySet().stream()
            .filter(entry -> entry.getKey().getUuid().equals(layoutUuid))
            .findFirst()
                .map(Map.Entry::getValue)
            .orElse(Collections.emptyList());
    }

    @Override
    public String toString() {
        return "SearchUrlContainer overriden toString() to pass through FTL context serialization.";
    }
}
