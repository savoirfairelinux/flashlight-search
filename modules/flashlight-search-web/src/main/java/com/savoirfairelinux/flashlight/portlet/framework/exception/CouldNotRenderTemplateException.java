package com.savoirfairelinux.flashlight.portlet.framework.exception;

import javax.portlet.PortletException;

/**
 * Thrown when a template cannot be rendered.
 */
public class CouldNotRenderTemplateException extends PortletException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     */
    public CouldNotRenderTemplateException(String message, Exception cause) {
        super(message, cause);
    }

}
