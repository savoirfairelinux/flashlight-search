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

/**
 * This facet filters document by classNameId
 */
public class ClassNameIdFacet extends MultiValueFacet {

    private static final String CLASS_NAME_ID = "classNameId";

    /**
     * Creates the facet with the given search context. Sets the field name to "classNameId" and makes the facet
     * static.
     *
     * @param searchContext The search context
     */
    public ClassNameIdFacet(SearchContext searchContext) {
        super(searchContext);
        this.setFieldName(CLASS_NAME_ID);
        this.setStatic(true);
    }

}
