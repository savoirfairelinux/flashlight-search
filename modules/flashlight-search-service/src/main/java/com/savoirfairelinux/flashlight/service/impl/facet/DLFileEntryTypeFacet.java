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

package com.savoirfairelinux.flashlight.service.impl.facet;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.savoirfairelinux.flashlight.service.impl.DocumentField;

/**
 * This facet is used to filter entries by their file entry type
 */
public class DLFileEntryTypeFacet extends MultiValueFacet {

    /**
     * Creates the facet. Sets the field name to "fileEntryTypeId" and makes the facet static
     *
     * @param searchContext The search context
     */
    public DLFileEntryTypeFacet(SearchContext searchContext) {
        super(searchContext);

        this.setFieldName(DocumentField.FILE_ENTRY_TYPE_ID.getName());
        this.setStatic(true);
    }

}
