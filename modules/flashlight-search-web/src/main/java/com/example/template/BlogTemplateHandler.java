package com.example.template;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;

import com.example.portlet.SearchPortletKeys;
import com.liferay.blogs.kernel.model.BlogsEntry;
import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.template.TemplateHandler;


@Component(
	    immediate = true,
	    property = {
	        "javax.portlet.name="+SearchPortletKeys.NAME
	    },
	    service = TemplateHandler.class)
public class BlogTemplateHandler extends BasePortletDisplayTemplateHandler{

	@Override
	public String getClassName() {
		
		return BlogsEntry.class.getName();
	}

	@Override
	public String getName(Locale locale) {
		
		return "Blog template";
	}

	@Override
	public String getResourceName() {
		
		return SearchPortletKeys.NAME+"";
	}

}
