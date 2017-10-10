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

package com.savoirfairelinux.flashlight.service.portlet.template;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;

/**
 * Contains the variables exposed in the view showing a journal article
 */
public enum JournalArticleViewTemplateContextVariable {

    REDIRECT(              "redirect",                                         String.class,                   "Redirect URL"),
    ASSET_RENDERER_FACTORY("rendererFactory",                               AssetRendererFactory.class,     "Asset renderer factory"),
    ASSET_RENDERER(        "renderer",                                         AssetRenderer.class,            "Asset renderer"),
    ASSET_ENTRY(           "entry",                                            AssetEntry.class,               "Asset entry");

    private String variableName;
    private Class<?> type;
    private String label;

    /**
     * Creates an enum value
     *
     * @param variableName The template variable's name
     * @param type The type of variable (used for the ADT)
     * @param label The label (used for the ADT)
     */
    private JournalArticleViewTemplateContextVariable(String variableName, Class<?> type, String label) {
        this.variableName = variableName;
        this.type = type;
        this.label = label;
    }

    /**
     * @return The name of the template variable
     */
    public String getVariableName() {
        return this.variableName;
    }

    /**
     * @return The type of the template variable
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * @return The variable's label
     */
    public String getLabel() {
        return this.label;
    }

}
