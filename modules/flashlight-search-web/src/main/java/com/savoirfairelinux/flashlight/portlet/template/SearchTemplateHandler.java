package com.savoirfairelinux.flashlight.portlet.template;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.template.TemplateHandler;
import com.liferay.portal.kernel.template.TemplateVariableGroup;
import com.savoirfairelinux.flashlight.portlet.FlashlightPortletKeys;

@Component(
	    immediate = true,
	    property = {
	        "javax.portlet.name="+FlashlightPortletKeys.PORTLET_NAME
	    },
	    service = TemplateHandler.class)
public class SearchTemplateHandler extends BasePortletDisplayTemplateHandler{

	@Override
	public String getClassName() {
		return Document.class.getName();
	}

	@Override
	public String getName(Locale locale) {
		return FlashlightPortletKeys.PORTLET_NAME;
	}

	@Override
	public String getResourceName() {
		return FlashlightPortletKeys.PORTLET_NAME;
	}
	
	@Override
	public Map<String, TemplateVariableGroup> getTemplateVariableGroups(
			long classPK, String language, Locale locale) throws Exception {

		Map<String, TemplateVariableGroup> templateVariableGroups = super
				.getTemplateVariableGroups(classPK, language, locale);

		TemplateVariableGroup templateVariableGroup = templateVariableGroups
				.get("fields");

		templateVariableGroup.empty();

		templateVariableGroup.addCollectionVariable("documents", List.class,
				"documents", "document", Document.class,
				"document", "title");

		return templateVariableGroups;
	}
	
}
