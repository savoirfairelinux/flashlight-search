package com.savoirfairelinux.flashlight.service.search.result.exception;

import com.liferay.portal.kernel.search.Document;

public class SearchResultProcessorException extends Exception {
    private static final long serialVersionUID = 1L;

    private static final String LOG_FORMAT = "Search result processor failure on document with UID \"%s\" : %s";

    /**
     * Creates the exception
     *
     * @param searchResult The search result that caused the failure
     * @param message The error message
     */
    public SearchResultProcessorException(Document searchResult, String message) {
        super(formatLogMessage(searchResult, message));
    }

    /**
     * Creates the exception
     *
     * @param cause The cause of the exception
     * @param searchResult The search result that caused the failure
     */
    public SearchResultProcessorException(Throwable cause, Document searchResult) {
        this(cause, searchResult, "No message given");
    }

    /**
     * Creates the exception
     *
     * @param cause The cause of the exception
     * @param searchResult The search result that caused the failure
     * @param message The error message
     */
    public SearchResultProcessorException(Throwable cause, Document searchResult, String message) {
        super(formatLogMessage(searchResult, message), cause);
    }

    private static String formatLogMessage(Document searchResult, String message) {
        return String.format(LOG_FORMAT, searchResult.getUID(), message);
    }

}
