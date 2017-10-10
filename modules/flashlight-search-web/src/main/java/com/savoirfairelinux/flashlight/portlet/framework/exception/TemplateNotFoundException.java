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

package com.savoirfairelinux.flashlight.portlet.framework.exception;

import javax.portlet.PortletException;

/**
 * Thrown when templates that must be rendered are not found
 */
public class TemplateNotFoundException extends PortletException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception
     * @param templatePath The template that was not found
     */
    public TemplateNotFoundException(String templatePath) {
        super("Template \""+ templatePath + "\" was not found.");
    }

}
