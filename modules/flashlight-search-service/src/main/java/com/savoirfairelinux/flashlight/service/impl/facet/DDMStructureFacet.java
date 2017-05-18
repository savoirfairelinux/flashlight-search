package com.savoirfairelinux.flashlight.service.impl.facet;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.savoirfairelinux.flashlight.service.impl.DocumentField;

/**
 * This facet filters document by DDM structure key
 */
public class DDMStructureFacet extends MultiValueFacet {

    public static final String CONFIG_FIELD_VALUES = "values";

    /**
     * Creates the facet with the given search context. Sets the field name to "ddmStructureKey" and makes the facet
     * static.
     *
     * @param searchContext The search context
     */
    public DDMStructureFacet(SearchContext searchContext) {
        super(searchContext);
        this.setFieldName(DocumentField.DDM_STRUCTURE_KEY.getName());
        this.setStatic(true);
    }

}
