package com.savoirfairelinux.flashlight.service.model;

import com.liferay.portal.kernel.model.Layout;

/**
 * Holds the page on which the search portlet is located, the URL to access the search portlet and the name of the
 * request parameter used to enter keywords
 */
public class SearchUrl {
    private static final String FORMAT_KEYWORDS_PARAM = "%skeywords";

    private Layout layout;
    private String url;
    private SearchUrlRequestParameter[] requestParameters;
    private String portletNamespace;

    /**
     * Creates the search URL container
     *
     * @param layout The page on which the search portlet is
     * @param url The URL to access the search portlet's search view
     * @param requestParameters The request parameters that must be sent to call the search portlet
     * @param portletNamespace The prefix to be prepended to the parameter names
     */
    public SearchUrl(Layout layout, String url, SearchUrlRequestParameter[] requestParameters, String portletNamespace) {
        this.layout = layout;
        this.url = url;
        this.requestParameters = requestParameters;
        this.portletNamespace = portletNamespace;
    }

    /**
     * @return The page on which the search portlet is
     */
    public Layout getLayout() {
        return this.layout;
    }

    /**
     * @return The URL to access the search portlet's search view
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @return The request parameters that must be sent to call the search portlet
     */
    public SearchUrlRequestParameter[] getRequestParameters() {
        return this.requestParameters;
    }

    /**
     * @return The request parameter used to enter keywords
     */
    public String getKeywordsParameter() {
        return String.format(FORMAT_KEYWORDS_PARAM, this.portletNamespace);
    }

    /**
     * @return The portlet namespace, to be prepended to the parameter names
     */
    public String getPortletNamespace() {
        return portletNamespace;
    }
}
