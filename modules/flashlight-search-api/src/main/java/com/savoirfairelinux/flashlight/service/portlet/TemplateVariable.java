package com.savoirfairelinux.flashlight.service.portlet;

/**
 * List of variables available from the portlet's rendering templates
 */
public enum TemplateVariable {

    THEME_DISPLAY("themeDisplayObj"),
    LOCALE("locale"),
    PORTLET_CONTEXT("portletContext"),
    REQUEST("request"),
    RESPONSE("response"),
    USER_INFO("userInfo");

    private String variableName;

    private TemplateVariable(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String toString() {
        return this.variableName;
    }

    /**
     * @return The name of the variable, as it is in the template
     */
    public String getVariableName() {
        return this.variableName;
    }

}
