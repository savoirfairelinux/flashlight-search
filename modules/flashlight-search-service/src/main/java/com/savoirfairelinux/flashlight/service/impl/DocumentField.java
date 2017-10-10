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

package com.savoirfairelinux.flashlight.service.impl;

import com.liferay.portal.kernel.search.Field;

/**
 * A list of search document fields not contained in {@link Field}
 */
public enum DocumentField {

    /**
     * A document's DDM structure key, if the document type supports it
     */
    DDM_STRUCTURE_KEY("ddmStructureKey"),
    FILE_ENTRY_TYPE_ID("fileEntryTypeId");

    private String name;

    /**
     * @param name The field's name
     */
    private DocumentField(String name) {
        this.name = name;
    }

    /**
     * @return The name of the field
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
