package com.savoirfairelinux.flashlight.service.portlet;

/**
 * List of different view modes in the portlet
 */
public enum ViewMode {

    RESULTS("results"),
    VIEW_JOURNAL("view-journal");

    private String paramValue;

    /**
     * Creates the view mode
     * @param paramValue The value of the HTTP parameter
     */
    private ViewMode(String paramValue) {
        this.paramValue = paramValue;
    }

    /**
     * @return The value of the mode's HTTP parameter
     */
    public String getParamValue() {
        return this.paramValue;
    }

    @Override
    public String toString() {
        return this.paramValue;
    }

    /**
     * Returns view mode corresponding to the given parameter value
     * @param paramValue The parameter value
     * @return A view mode corresponding to the given parameter value, defaulting to RESULTS
     */
    public static ViewMode getViewMode(String paramValue) {
        ViewMode returnedMode = RESULTS;
        ViewMode[] modes = ViewMode.values();
        int modesLength = modes.length;
        boolean foundMode = false;

        for(int i = 0; !foundMode && i < modesLength; i++) {
            if(modes[i].getParamValue().equals(paramValue)) {
                returnedMode = modes[i];
                foundMode = true;
            }
        }

        return returnedMode;
    }

}
