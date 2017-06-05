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
