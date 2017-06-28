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

    private PortletRequestParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
