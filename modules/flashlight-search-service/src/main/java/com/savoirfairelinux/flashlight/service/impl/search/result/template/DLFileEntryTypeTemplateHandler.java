package com.savoirfairelinux.flashlight.service.impl.search.result.template;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.template.TemplateHandler;
import com.liferay.portal.kernel.template.TemplateVariableGroup;
import com.liferay.portal.kernel.util.Portal;
import com.savoirfairelinux.flashlight.service.FlashlightSearchPortletKeys;

@Component(
    immediate = true,
    property = {
        "javax.portlet.name=" + FlashlightSearchPortletKeys.PORTLET_NAME
    },
    service = TemplateHandler.class)
public class DLFileEntryTypeTemplateHandler extends BasePortletDisplayTemplateHandler {

    private static final String VAR_GROUP_NAME = "viewContextVariables";
    private static final String VAR_GROUP_LABEL = "Context";

    private static final String LANGUAGE_KEY_TEMPLATE = "DLFileEntry search result template";

    @Reference
    private Portal portal;

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getName(Locale locale) {
        return LanguageUtil.get(locale, LANGUAGE_KEY_TEMPLATE);
    }

    @Override
    public String getResourceName() {
        return FlashlightSearchPortletKeys.PORTLET_NAME;
    }

    @Override
    public Map<String, TemplateVariableGroup> getTemplateVariableGroups(long classPK, String language, Locale locale) throws Exception {
        Map<String, TemplateVariableGroup> templateVariableGroups = new LinkedHashMap<>();
        TemplateVariableGroup mainGroup = new TemplateVariableGroup(VAR_GROUP_LABEL);
        templateVariableGroups.put(VAR_GROUP_NAME, mainGroup);
        return templateVariableGroups;
    }
}
