package com.savoirfairelinux.flashlight.service.portlet;

/**
 * Lists the different available edit modes in the portlet
 */
public enum EditMode {

    GLOBAL("global"),
    TAB("tab"),
    FACET("facet");

    private String paramValue;

    /**
     * Creates the edit mode
     * @param paramValue The value of the HTTP parameter
     */
    private EditMode(String paramValue) {
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
     * Returns the edit mode corresponding to the given parameter value
     * @param paramValue The parameter value
     * @return An edit mode corresponding to the given parameter value, defaulting to GLOBAL
     */
    public static EditMode getEditMode(String paramValue) {
        EditMode returnedMode = GLOBAL;
        EditMode[] modes = EditMode.values();
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
