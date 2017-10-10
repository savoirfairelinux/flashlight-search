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

package com.savoirfairelinux.flashlight.portlet;

import javax.portlet.ResourceRequest;

/**
 * Lists the available resource that can be accessed from the portlet's serveResource method
 */
public enum PortletResource {

    LOAD_MORE("load-more");

    private String resourceId;

    private PortletResource(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return this.resourceId;
    }

    @Override
    public String toString() {
        return this.resourceId;
    }

    /**
     * Returns the portlet resource that corresponds to the resource id contained in the request
     *
     * @param request The resource request
     * @return The corresponding resource or null if none found
     */
    public static PortletResource getResource(ResourceRequest request) {
        PortletResource resource = null;
        String resourceId = request.getResourceID();

        PortletResource[] resources = PortletResource.values();
        int resourceLength = resources.length;

        for(int i = 0; resource == null && i < resourceLength; i++) {
            if(resources[i].getResourceId().equals(resourceId)) {
                resource = resources[i];
            }
        }

        return resource;
    }

}
