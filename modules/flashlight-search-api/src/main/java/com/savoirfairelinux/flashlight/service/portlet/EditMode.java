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
