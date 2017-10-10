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

package com.savoirfairelinux.flashlight.service.search.result.exception;

import com.liferay.portal.kernel.search.Document;

/**
 * These exceptions are thrown when a search result processor fails to correctly process a search result. The exception
 * should give information about which indexed document caused the error and why the processor failed to work.
 */
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
