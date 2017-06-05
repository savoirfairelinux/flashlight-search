package com.savoirfairelinux.flashlight.service;

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
