package com.example.template;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;

import com.example.portlet.SearchPortletKeys;
import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.template.TemplateHandler;
import com.liferay.bookmarks.model.BookmarksEntry;

@Component(
	    immediate = true,
	    property = {
	        "javax.portlet.name="+SearchPortletKeys.NAME
	    },
	    service = TemplateHandler.class)
public class BookmarkTemplateHandler extends BasePortletDisplayTemplateHandler{


	@Override
	public String getClassName() {
		
		return BookmarksEntry.class.getName();
	}

	@Override
	public String getName(Locale locale) {
		
		return "Bookmark template";
	}

	@Override
	public String getResourceName() {
		
		return SearchPortletKeys.NAME+"";
	}
}
