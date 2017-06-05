package com.savoirfairelinux.flashlight.service.impl;

import com.liferay.portal.kernel.search.Field;

/**
 * A list of search document fields not contained in {@link Field}
 */
public enum DocumentField {

    /**
     * A document's DDM structure key, if the document type supports it
     */
    DDM_STRUCTURE_KEY("ddmStructureKey");

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
