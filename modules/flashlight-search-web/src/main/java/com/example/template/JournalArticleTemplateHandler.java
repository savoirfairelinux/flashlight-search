package com.example.template;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;

import com.example.portlet.SearchPortletKeys;
import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.template.TemplateHandler;
import com.liferay.journal.model.JournalArticle;

@Component(
	    immediate = true,
	    property = {
	        "javax.portlet.name="+SearchPortletKeys.NAME
	    },
	    service = TemplateHandler.class)
public class JournalArticleTemplateHandler extends BasePortletDisplayTemplateHandler{
	

	@Override
	public String getClassName() {
		
		return JournalArticle.class.getName();
	}

	@Override
	public String getName(Locale locale) {
		
		return "Journal Article template";
	}

	@Override
	public String getResourceName() {
		
		return SearchPortletKeys.NAME+"";
	}

}
