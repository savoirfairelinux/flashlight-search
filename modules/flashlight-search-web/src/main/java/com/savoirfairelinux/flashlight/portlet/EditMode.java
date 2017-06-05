package com.savoirfairelinux.flashlight.portlet;

/**
 * Lists the different available edit modes in the portlet
 */
public enum EditMode {

    GLOBAL("global"),
    TAB("tab"),
    FACET("facet");

    private String paramValue;

    private EditMode(String paramValue) {
        this.paramValue = paramValue;
    }

    /**
     * @return The mode's textual form value
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
        EditMode returnedMode = EditMode.GLOBAL;
        EditMode[] modes = EditMode.values();
        int modesLength = modes.length;
        for(int i = 0; returnedMode == EditMode.GLOBAL && i < modesLength; i++) {
            if(modes[i].getParamValue().equals(paramValue)) {
                returnedMode = modes[i];
            }
        }
        return returnedMode;
    }

}
