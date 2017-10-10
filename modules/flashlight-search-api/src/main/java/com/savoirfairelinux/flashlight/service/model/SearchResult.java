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

package com.savoirfairelinux.flashlight.service.model;

/**
 * Represents a single search result
 */
public class SearchResult {

    private String viewUrl;
    private String rendering;
    private String title;

    /**
     * Creates a search result
     * @param rendering The result's templated rendering
     * @param viewUrl The result's full content view URL or null if it is the template's job to generate the URL
     * @param title The result's title
     */
    public SearchResult(String rendering, String viewUrl, String title) {
        this.viewUrl = viewUrl;
        this.rendering = rendering;
        this.title = title;
    }

    /**
     * @return The result's full content view URL or null if it is the template's job to generate the URL
     */
    public String getViewUrl() {
        return this.viewUrl;
    }

    /**
     * @return The result's templated rendering
     */
    public String getRendering() {
        return this.rendering;
    }

    /**
     * @return The result's title
     */
    public String getTitle() {
        return this.title;
    }

}
