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
