package com.savoirfairelinux.flashlight.action;

import com.liferay.portal.kernel.model.Layout;

/**
 * Holds the page on which the search portlet is located, the URL to access the search portlet and the name of the
 * request parameter used to enter keywords
 */
public class SearchUrl {

    private Layout layout;
    private String url;
    private RequestParameter[] requestParameters;
    private String portletNamespace;

    /**
     * Creates the search URL container
     *
     * @param layout The page on which the search portlet is
     * @param url The URL to access the search portlet's search view
     * @param requestParameters The request parameters that must be sent to call the search portlet
     * @param portletNamespace The prefix to be prepended to the parameter names
     */
    public SearchUrl(Layout layout, String url, RequestParameter[] requestParameters, String portletNamespace) {
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
    public RequestParameter[] getRequestParameters() {
        return this.requestParameters;
    }

    /**
     * @return The request parameter used to enter keywords
     */
    public String getKeywordsParameter() {
        return this.portletNamespace + "keywords";
    }

    /**
     * @return The portlet namespace, to be prepended to the parameter names
     */
    public String getPortletNamespace() {
        return portletNamespace;
    }
}
