package com.savoirfairelinux.flashlight.service.model;

/**
 * Represents a single search result
 */
public class SearchResult {

    private String viewUrl;
    private String rendering;
    private String title;

    public SearchResult(String rendering, String viewUrl, String title) {
        this.viewUrl = viewUrl;
        this.rendering = rendering;
        this.title = title;
    }

    /**
     * @return The result's full content view URL or null if it is the template's job to generate the URL
     */
    public String getViewUrl() {
        return this.viewUrl;
    }

    /**
     * @return The result's templated rendering
     */
    public String getRendering() {
        return this.rendering;
    }

    /**
     * @return The result's title
     */
    public String getTitle() {
        return this.title;
    }

}
