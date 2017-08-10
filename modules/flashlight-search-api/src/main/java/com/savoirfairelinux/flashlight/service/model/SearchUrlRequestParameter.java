package com.savoirfairelinux.flashlight.service.model;

/**
 * Holds request parameters that must be passed in the URL to call the search portlet
 */
public class SearchUrlRequestParameter {

    private String name;
    private String value;

    public SearchUrlRequestParameter(String name, String value) {
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
