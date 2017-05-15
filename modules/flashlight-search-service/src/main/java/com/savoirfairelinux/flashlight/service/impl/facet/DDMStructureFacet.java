package com.savoirfairelinux.flashlight.service.impl.facet;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.savoirfairelinux.flashlight.service.impl.DocumentField;

public class DDMStructureFacet extends MultiValueFacet {

    public DDMStructureFacet(SearchContext searchContext) {
        super(searchContext);
        this.setFieldName(DocumentField.DDM_STRUCTURE_KEY.getName());
        this.setStatic(false);
    }

}
