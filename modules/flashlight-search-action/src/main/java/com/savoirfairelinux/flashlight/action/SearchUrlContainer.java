package com.savoirfairelinux.flashlight.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.liferay.portal.kernel.model.Layout;

public class SearchUrlContainer {
    private final Map<Layout, List<SearchUrl>> searchUrls;

    public SearchUrlContainer(Map<Layout, List<SearchUrl>> searchUrls) {
        this.searchUrls = searchUrls;
    }

    public Map<Layout, List<SearchUrl>> getSearchUrls() {
        return searchUrls;
    }

    public List<SearchUrl> getSearchUrlByLayoutUuid(String layoutUuid) {
        return this.searchUrls.entrySet().stream()
            .filter(entry -> entry.getKey().getUuid().equals(layoutUuid))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(Collections.emptyList());
    }
}
