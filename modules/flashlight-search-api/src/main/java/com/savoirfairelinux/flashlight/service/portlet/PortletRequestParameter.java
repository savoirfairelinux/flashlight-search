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
 * Lists the parameters commonly used by the portlet's search view
 */
public enum PortletRequestParameter {

    VIEW_MODE("view-mode"),
    EDIT_MODE("edit-mode"),
    KEYWORDS("keywords"),
    TAB_ID("tab-id"),
    ENTRY_ID("entry-id"),
    PAGE_OFFSET("page-offset"),
    REDIRECT("redirect"),
    RANDOM_CACHE("rand");

    private String name;

    /**
     * Creates an enum value
     * @param name The parameter name
     */
    private PortletRequestParameter(String name) {
        this.name = name;
    }

    /**
     * @return The parameter name
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
