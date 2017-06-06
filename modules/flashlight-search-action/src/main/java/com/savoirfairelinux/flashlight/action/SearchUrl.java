package com.savoirfairelinux.flashlight.action;

import javax.portlet.PortletURL;

import com.liferay.portal.kernel.model.Layout;

/**
 * Holds the page on which the search portlet is located, the URL to access the search portlet and the name of the
 * request parameter used to enter keywords
 */
public class SearchUrl {

    private Layout layout;
    private PortletURL url;
    private RequestParameter[] requestParameters;
    private String keywordsParameter;

    /**
     * Creates the search URL container
     *
     * @param layout The page on which the search portlet is
     * @param url The URL to access the search portlet's search view
     * @param requestParameters The request parameters that must be sent to call the search portlet
     * @param keywordsParameter The request parameter used to enter keywords
     */
    public SearchUrl(Layout layout, PortletURL url, RequestParameter[] requestParameters, String keywordsParameter) {
        this.layout = layout;
        this.url = url;
        this.requestParameters = requestParameters;
        this.keywordsParameter = keywordsParameter;
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
    public PortletURL getUrl() {
        return this.url;
    }

    /**
     * @return The request parameters that must be sent to call the search portlet
     */
    public RequestParameter[] getRequestParameters() {
        return requestParameters;
    }

    /**
     * @return The request parameter used to enter keywords
     */
    public String getKeywordsParameter() {
        return this.keywordsParameter;
    }


}
