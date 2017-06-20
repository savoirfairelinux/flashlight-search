package com.savoirfairelinux.flashlight.service.impl.search.result.template;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.template.TemplateHandler;
import com.liferay.portal.kernel.template.TemplateVariableGroup;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.savoirfairelinux.flashlight.service.FlashlightSearchPortletKeys;

/**
 * Template handler used to render DLFileEntry search results in Flashlight
 */
@Component(
    immediate = true,
    property = {
        "javax.portlet.name=" + FlashlightSearchPortletKeys.PORTLET_NAME
    },
    service = TemplateHandler.class)
public class DLFileEntryTypeTemplateHandler extends BasePortletDisplayTemplateHandler {

    private static final String CLASS_NAME = DLFileEntryTypeTemplateHandler.class.getName();
    private static final String VAR_GROUP_NAME = "viewContextVariables";
    private static final String VAR_GROUP_LABEL = "File";

    private static final String RES_BUNDLE = "content.Language";
    private static final String LANGUAGE_KEY_TEMPLATE = "template.name";

    @Reference
    private Portal portal;

    @Reference
    private Language language;

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public String getName(Locale locale) {
        ResourceBundle bundle = ResourceBundleUtil.getBundle(RES_BUNDLE, locale, this.getClass());
        return this.language.get(bundle, LANGUAGE_KEY_TEMPLATE);
    }

    @Override
    public String getResourceName() {
        return FlashlightSearchPortletKeys.PORTLET_NAME;
    }

    @Override
    public Map<String, TemplateVariableGroup> getTemplateVariableGroups(long classPK, String language, Locale locale) throws Exception {
        Map<String, TemplateVariableGroup> templateVariableGroups = new LinkedHashMap<>();
        TemplateVariableGroup mainGroup = new TemplateVariableGroup(VAR_GROUP_LABEL);
        for(DLFileEntryTemplateVariable variable : DLFileEntryTemplateVariable.values()) {
            mainGroup.addVariable(variable.getLabel(), variable.getType(), variable.getVariableName());
        }
        templateVariableGroups.put(VAR_GROUP_NAME, mainGroup);
        return templateVariableGroups;
    }
}
