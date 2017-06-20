package com.savoirfairelinux.flashlight.service.impl.search.result.template;

import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;

/**
 * Contains the DLFileEntry search result processor ADT variables
 */
public enum DLFileEntryTemplateVariable {

    FILE_ENTRY("fileEntry", FileEntry.class, "File entry object"),
    FILE_VERSION("fileVersion", FileVersion.class, "File version object"),
    FILE_URL("fileUrl", String.class, "File download URL"),
    FILE_PREVIEW_URL("filePreviewUrl", String.class, "File preview URL"),
    FILE_IMAGE_PREVIEW_URL("fileImagePreviewUrl", String.class, "Image preview URL");

    private String variableName;
    private Class<?> type;
    private String label;

    /**
     * @param variableName The variable name
     * @param type The variable type
     * @param label The variable label
     */
    private DLFileEntryTemplateVariable(String variableName, Class<?> type, String label) {
        this.variableName = variableName;
        this.type = type;
        this.label = label;
    }

    /**
     * @return The variable name
     */
    public String getVariableName() {
        return this.variableName;
    }

    /**
     * @return The variable type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * @return The variable label
     */
    public String getLabel() {
        return label;
    }

}
