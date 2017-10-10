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
