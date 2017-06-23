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
