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
 * Contains metadata about the portlet
 */
public class FlashlightSearchPortletKeys {

    /**
     * The portlet's name, as indicated in the portlet declaration
     */
    public static final String PORTLET_NAME = "com_savoirfairelinux_flashlight_portlet_FlashlightSearchPortlet";

    /**
     * Construction prevention
     * @throws Exception When called
     */
    private FlashlightSearchPortletKeys() throws Exception {
        throw new Exception("Constants class. Do not construct.");
    }

}
