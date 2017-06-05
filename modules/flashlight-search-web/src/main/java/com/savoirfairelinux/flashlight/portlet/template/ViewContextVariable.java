package com.savoirfairelinux.flashlight.portlet.template;

import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.savoirfairelinux.flashlight.portlet.framework.TemplateVariable;
import com.savoirfairelinux.flashlight.service.model.SearchResultsContainer;

/**
 * Contains the variables exposed in the portlet's view (and, by extention, its ADTs)
 */
public enum ViewContextVariable {

    // From the framework
    LOCALE(             TemplateVariable.LOCALE.getVariableName(),          Locale.class,                   "Current locale"),
    THEME_DISPLAY(      TemplateVariable.THEME_DISPLAY.getVariableName(),   ThemeDisplay.class,             "ThemeDisplay"),
    REQUEST(            TemplateVariable.REQUEST.getVariableName(),         PortletRequest.class,           "Portlet request"),
    RESPONSE(           TemplateVariable.RESPONSE.getVariableName(),        PortletResponse.class,          "Portlet response"),
    USER_INFO(          TemplateVariable.USER_INFO.getVariableName(),       Map.class,                      "User info map"),

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
