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

package com.savoirfairelinux.flashlight.service.portlet.template;

import java.util.Map;
import java.util.function.BiFunction;

import com.savoirfairelinux.flashlight.service.model.SearchResultsContainer;

/**
 * Contains the variables exposed in the portlet's view (and, by extention, its ADTs)
 */
public enum ViewContextVariable {

    // From the portlet itself
    NAMESPACE(          "ns",                                               String.class,                   "Portlet namespace"),
    KEYWORD_URL(        "keywordUrl",                                       String.class,                   "Search keyword URL"),
    KEYWORDS(           "keywords",                                         String.class,                   "Search keywords"),
    TAB_URLS(           "tabUrls",                                          Map.class,                      "Tab view URL mapping"),
    LOAD_MORE_URLS(     "loadMoreUrls",                                     Map.class,                      "Load more URL mapping"),
    RESULTS_CONTAINER(  "resultsContainer",                                 SearchResultsContainer.class,   "Search results"),
    TAB_ID(             "tabId",                                            String.class,                   "Selected tab ID"),
    FORMAT_FACET_TERM(  "facetTerm",                                        BiFunction.class,               "Format facet term"),
    JAVASCRIPT_PATH(    "javaScriptPath",                                   String.class,                   "Flashlight JavaScript path");

    private String variableName;
    private Class<?> type;
    private String label;

    /**
     * Creates an enum value
     *
     * @param variableName The template variable's name
     * @param type The type of variable (used for the ADT)
     * @param label The label (used for the ADT)
     */
    private ViewContextVariable(String variableName, Class<?> type, String label) {
        this.variableName = variableName;
        this.type = type;
        this.label = label;
    }

    /**
     * @return The name of the template variable
     */
    public String getVariableName() {
        return this.variableName;
    }

    /**
     * @return The type of the template variable
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * @return The variable's label
     */
    public String getLabel() {
        return this.label;
    }

}
