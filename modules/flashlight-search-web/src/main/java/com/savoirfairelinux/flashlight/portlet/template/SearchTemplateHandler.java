package com.savoirfairelinux.flashlight.portlet.template;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.template.TemplateHandler;
import com.liferay.portal.kernel.template.TemplateVariableGroup;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.savoirfairelinux.flashlight.service.FlashlightSearchPortletKeys;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;

@Component(
    immediate = true,
    property = {
        "javax.portlet.name=" + FlashlightSearchPortletKeys.PORTLET_NAME
    },
    service = TemplateHandler.class)
public class SearchTemplateHandler extends BasePortletDisplayTemplateHandler {

    private static final String VAR_GROUP_NAME = "viewContextVariables";
    private static final String VAR_GROUP_LABEL = "Context";

    private static final String LANGUAGE_BUNDLE = "content.Language";
    private static final String LANGUAGE_KEY_TEMPLATE = "template";

    @Reference
    private Portal portal;

    @Override
    public String getClassName() {
        return FlashlightSearchService.ADT_CLASS.getName();
    }

    @Override
    public String getName(Locale locale) {
        ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(LANGUAGE_BUNDLE, locale, getClass());
        String portletTitle = portal.getPortletTitle(FlashlightSearchPortletKeys.PORTLET_NAME, resourceBundle);
        return portletTitle.concat(StringPool.SPACE).concat(LanguageUtil.get(locale, LANGUAGE_KEY_TEMPLATE));
    }

    @Override
    public String getResourceName() {
        return FlashlightSearchPortletKeys.PORTLET_NAME;
    }

    @Override
    public Map<String, TemplateVariableGroup> getTemplateVariableGroups(long classPK, String language, Locale locale) throws Exception {
        Map<String, TemplateVariableGroup> templateVariableGroups = new LinkedHashMap<>();
        TemplateVariableGroup mainGroup = new TemplateVariableGroup(VAR_GROUP_LABEL);
        for(ViewContextVariable variable : ViewContextVariable.values()) {
            mainGroup.addVariable(variable.getLabel(), variable.getType(), variable.getVariableName());
        }
        templateVariableGroups.put(VAR_GROUP_NAME, mainGroup);
        return templateVariableGroups;
    }
}
