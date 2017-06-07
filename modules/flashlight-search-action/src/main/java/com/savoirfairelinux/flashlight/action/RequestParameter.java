package com.savoirfairelinux.flashlight.action;

/**
 * Holds request parameters that must be passed in the URL to call the search portlet
 */
public class RequestParameter {

    private String name;
    private String value;

    public RequestParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

}
