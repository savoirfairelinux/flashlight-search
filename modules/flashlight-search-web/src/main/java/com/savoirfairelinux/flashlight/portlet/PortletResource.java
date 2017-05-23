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
