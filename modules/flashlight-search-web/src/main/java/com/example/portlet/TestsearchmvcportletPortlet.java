package com.example.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.example.configuration.SearchConfiguration;
import com.example.portlet.searchdisplay.SearchDisplay;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalArticleDisplay;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.PortletRequestModel;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringUtil;
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
      /* System.out.println("----------------");
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
		
		
		
		//hits = display.customSearch(renderRequest, keywords, renderRequest.getPreferences());
		//documents = hits.toList();
		
		
		 //groupedDocuments = display.groupedsearch(renderRequest, keywords, renderRequest.getPreferences());
		 groupedDocuments = display.customGroupedSearch(renderRequest, keywords, renderRequest.getPreferences(),Field.ENTRY_CLASS_NAME);
		 //groupedDocuments = display.customGroupedSearch(renderRequest, keywords, renderRequest.getPreferences(),Field.ENTRY_CLASS_NAME , 3);
		 
		 for(String key : groupedDocuments.keySet()){
				System.out.println("key : "+ key);
				
			}
			System.out.println("----------------");
		
	} catch (Exception e) {
		e.printStackTrace();
	}
       }
       
       //Map<String, String> facets = getFacets();
       Map<String, String> facets = getFacetsDefinitions(scopeGroupId, themeDisplay.getCompanyId());
       long[] defaulIds =new long[facets.size()+1];
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
       renderRequest.setAttribute("facets", facets);
       System.out.println(facets.size());
       
       //renderRequest.setAttribute("facets", SearchDisplay.getEntryClassNames());
       
       
       
       
    /*   JournalArticleDisplay articleDisplay = testAtricleDisplay(renderRequest, renderResponse, 31430);
       System.out.println("display title : "+articleDisplay.getTitle());
       System.out.println("display content : " + articleDisplay.getContent());
       
       renderRequest.setAttribute("articleDisplay", articleDisplay );*/
       
       



        super.render(renderRequest, renderResponse);
    }
	
	@Activate
    @Modified
    protected void activate(Map<Object, Object> properties) {
            searchConfiguration = Configurable.createConfigurable(
                    SearchConfiguration.class, properties);
    }
	
	
	protected JournalArticleDisplay testAtricleDisplay(RenderRequest renderRequest, 
            RenderResponse renderResponse , long id){
		JournalArticle article = null;
		JournalArticleDisplay articleDisplay =null;
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		try {
			 article = JournalArticleLocalServiceUtil.getArticle(id);
			articleDisplay =
				JournalArticleLocalServiceUtil.getArticleDisplay(
					article, article.getDDMTemplateKey(), "", themeDisplay.getLanguageId(), 1,
					new PortletRequestModel(
							renderRequest, renderResponse),
					themeDisplay);
		}
		catch (PortalException pe) {
			
		}
		return articleDisplay;
	}
	
	protected Map<String,String> getFacets(){
		
		if(_facets == null){
		_facets = new HashMap<String,String>();
		_facets.put("31400", "first structure");
		_facets.put("31404", "second structure");
		_facets.put("BASIC-WEB-CONTENT", "Basic web content");
		_facets.put("29218", "Contracts");
		}
		
		return _facets;
		
	}
	
	protected Map<String,String> getFacetsDefinitions(long groupid , long companyId){
		Map<String,String> facets = new HashMap<String,String>();
		facets.put("BASIC-WEB-CONTENT" , "Basic web content");
		List<DDMStructure> structures = DDMStructureLocalServiceUtil.getStructures(groupid);
		System.out.println("number of structure facets : " + structures.size());
		for(DDMStructure structure : structures){
			String name = structure.getName("en_US");
			String key = structure.getStructureKey();
			System.out.println(key +" :  " + name);
			try{
			Long.parseLong(key);
			facets.put(key, name);
			}
			catch(Exception e){
				
			}
		}
		
		//List<DLFileEntryType> filetypes  = DLFileEntryTypeLocalServiceUtil.getFileEntryTypes(new long[]{0,groupid});
		List<DLFileEntryType> filetypes  = DLFileEntryTypeLocalServiceUtil.getDLFileEntryTypes(0, 50);
		
		System.out.println("number of filetype facets : " + filetypes.size());
		for(DLFileEntryType filetype : filetypes){
			String key = filetype.getFileEntryTypeId()+"";
			String name = filetype.getName("en_US");
			System.out.println(key +" :  " + name);
			facets.put(key, name);
		}
		
		return facets;
	}


	
	private Map<String,String> _facets;
    private volatile SearchConfiguration searchConfiguration;
}