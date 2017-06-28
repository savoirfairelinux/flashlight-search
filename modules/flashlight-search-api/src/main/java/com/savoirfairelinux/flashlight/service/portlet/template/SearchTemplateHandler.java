package com.savoirfairelinux.flashlight.service.portlet.template;

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
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;
import com.savoirfairelinux.flashlight.service.portlet.FlashlightSearchPortletKeys;

/**
 * Template handler used for the portlet's view ADT
 */
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
    private static final String LANGUAGE_KEY_TEMPLATE = "template.search";

    private static final String VAR_GROUP_FIELDS = "fields";

    @Reference
    private Portal portal;

    @Reference
    private Language language;

    @Override
    public String getClassName() {
        return FlashlightSearchService.ADT_CLASS.getName();
    }

    @Override
    public String getName(Locale locale) {
        ResourceBundle bundle = ResourceBundleUtil.getBundle(LANGUAGE_BUNDLE, locale, this.getClass());
        return this.language.get(bundle, LANGUAGE_KEY_TEMPLATE);
    }

    @Override
    public String getResourceName() {
        return FlashlightSearchPortletKeys.PORTLET_NAME;
    }

    @Override
    public Map<String, TemplateVariableGroup> getTemplateVariableGroups(long classPK, String language, Locale locale) throws Exception {
        Map<String, TemplateVariableGroup> templateVariableGroups = super.getTemplateVariableGroups(classPK, language, locale);
        templateVariableGroups.remove(VAR_GROUP_FIELDS);
        TemplateVariableGroup mainGroup = new TemplateVariableGroup(VAR_GROUP_LABEL);
        for(ViewContextVariable variable : ViewContextVariable.values()) {
            mainGroup.addVariable(variable.getLabel(), variable.getType(), variable.getVariableName());
        }
        templateVariableGroups.put(VAR_GROUP_NAME, mainGroup);
        return templateVariableGroups;
    }
}
