// Copyright 2017 Savoir-faire Linux
// This file is part of Flashlight Search.

// Flashlight Search is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Flashlight Search is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Flashlight Search.  If not, see <http://www.gnu.org/licenses/>.

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
     * @return The portlet's namespace
     */
    public String getPortletNamespace() {
        return portletNamespace;
    }
}
