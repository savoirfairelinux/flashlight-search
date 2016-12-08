package com.example.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.example.configuration.SearchConfiguration;
import com.example.portlet.searchdisplay.SearchDisplay;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.util.bridges.freemarker.FreeMarkerPortlet;

import aQute.bnd.annotation.metatype.Configurable;

@Component(
	configurationPid ="com.example.configuration.SearchConfiguration",
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.instanceable=false",
		"javax.portlet.display-name=Flashlight Portlet",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.ftl",
		"javax.portlet.init-param.config-template=/configuration.jsp",
		"javax.portlet.init-param.edit-template=/configuration.ftl",
		"javax.portlet.portlet-mode=text/html;view,edit",
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.name="+SearchPortletKeys.NAME
	},
	service = Portlet.class
)
public class TestsearchmvcportletPortlet extends FreeMarkerPortlet {
	public TestsearchmvcportletPortlet(){
		
	}

	
	
	@Override
    public void render(RenderRequest renderRequest, 
                    RenderResponse renderResponse)
            throws IOException, PortletException
    {
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		long scopeGroupId = themeDisplay.getScopeGroupId();
		String keywords = renderRequest.getParameter("keywords");
       System.out.println("you are searching for " + keywords);
       SearchDisplay display = new SearchDisplay();
       Hits hits;
       List<Document> documents= new ArrayList<Document>();
       Map<String, List<Document>> groupedDocuments = null;
       /*System.out.println("----------------");
       Map<String, String[]> map = renderRequest.getParameterMap();
		for(String key : map.keySet()){
			System.out.println("key : "+ key);
			for(String value : map.get(key) ){
				System.out.println("values are  : " +value);
			}
		}
		System.out.println("----------------");*/
       if(keywords  !=null){
	try {
		
		
		
		hits = display.customSearch(renderRequest, keywords, renderRequest.getPreferences());
		documents = hits.toList();
		
		
		 //groupedDocuments = display.groupedsearch(renderRequest, keywords, renderRequest.getPreferences());
		 groupedDocuments = display.customGroupedSearch(renderRequest, keywords, renderRequest.getPreferences(),Field.ENTRY_CLASS_NAME);
		 
		
	} catch (Exception e) {
		e.printStackTrace();
	}
       }
       
       
       long[] defaulIds =new long[SearchDisplay.getEntryClassNames().length+1];
       String[] defaults=new String[defaulIds.length];
       for(int i=0;i<defaulIds.length;i++){
    	   defaulIds[i] = scopeGroupId;
    	   defaults[i] = "default";
       }
       String[] displayStyle = renderRequest.getPreferences().getValues("displayStyle",defaults);
       long[] displayStyleGroupId = GetterUtil.getLongValues(renderRequest.getPreferences().getValues("displayStyleGroupId",null),defaulIds);
       
       renderRequest.setAttribute("displayStyle", displayStyle);
       renderRequest.setAttribute("displayStyleGroupId", displayStyleGroupId);
       renderRequest.setAttribute("documentClassName", Document.class.getName());
       renderRequest.setAttribute("documents", documents);
       renderRequest.setAttribute("groupedDocuments", groupedDocuments);
       renderRequest.setAttribute("facets", SearchDisplay.getEntryClassNames());



        super.render(renderRequest, renderResponse);
    }
	
	@Activate
    @Modified
    protected void activate(Map<Object, Object> properties) {
            searchConfiguration = Configurable.createConfigurable(
                    SearchConfiguration.class, properties);
    }
	


	

    private volatile SearchConfiguration searchConfiguration;
}