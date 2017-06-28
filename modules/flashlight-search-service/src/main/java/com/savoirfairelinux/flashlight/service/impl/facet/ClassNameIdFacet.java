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
