package com.savoirfairelinux.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.blogs.kernel.model.BlogsEntry;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletRequestModel;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.util.bridges.freemarker.FreeMarkerPortlet;
import com.savoirfairelinux.portlet.searchdisplay.SearchDisplay;
import com.savoirfairelinux.portlet.searchdisplay.SearchResultWrapper;

@Component(immediate = true, property = { 
				"com.liferay.portlet.display-category=category.sample",
				"com.liferay.portlet.instanceable=false", 
				"javax.portlet.display-name=Flashlight Portlet",
				"javax.portlet.init-param.template-path=/",
				"javax.portlet.init-param.view-template=/view.ftl",
				"javax.portlet.init-param.edit-template=/configuration.ftl", 
				"javax.portlet.portlet-mode=text/html;view,edit",
				"javax.portlet.resource-bundle=content.Language",
				"javax.portlet.security-role-ref=power-user,user",
				"javax.portlet.name=" + SearchPortletKeys.NAME }, 
			service = Portlet.class)
public class FlashlightSearchPortlet extends FreeMarkerPortlet {
	
	private static final Log LOG = LogFactoryUtil.getLog(FlashlightSearchPortlet.class);
	
	public FlashlightSearchPortlet() {

	}

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		long scopeGroupId = themeDisplay.getScopeGroupId();
		String keywords = renderRequest.getParameter("keywords");

		SearchDisplay display = new SearchDisplay(renderRequest.getPreferences());
		Map<String, List<Document>> groupedDocuments = null;
		List<SearchResultWrapper> results = new ArrayList<SearchResultWrapper>();
		String[] enabled_facets = renderRequest.getPreferences().getValues("facets", new String[0]);

		if (keywords != null) {
			try {

				groupedDocuments = display.customGroupedSearch(renderRequest, keywords, renderRequest.getPreferences(),
						Field.ENTRY_CLASS_NAME);
				for(String key : groupedDocuments.keySet()){
					results.add(new SearchResultWrapper(key,groupedDocuments.get(key)));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		List<SearchFacet> searchFacets = display.getEnabledSearchFacets();

		renderRequest.setAttribute("searchFacets", searchFacets);

		Map<String, String> facets = getFacetsDefinitions(scopeGroupId, themeDisplay.getCompanyId());
		long displayStyleGroupId = GetterUtil
				.getLong(renderRequest.getPreferences().getValue("displayStyleGroupId", ""), scopeGroupId);

		renderRequest.setAttribute("displayStyleGroupId", displayStyleGroupId);
		renderRequest.setAttribute("documentClassName", Document.class.getName());
		renderRequest.setAttribute("groupedDocuments", groupedDocuments);
		renderRequest.setAttribute("facets", facets);
		renderRequest.setAttribute("keywords", keywords);
		renderRequest.setAttribute("enabled_facets", enabled_facets);
		renderRequest.setAttribute("categories", AssetCategoryLocalServiceUtil.getCategories());
		renderRequest.setAttribute("searchResults", results);
		renderRequest.setAttribute("themeDisplay", themeDisplay);
		renderRequest.setAttribute("portletRequest", new PortletRequestModel(renderRequest, renderResponse));
		renderRequest.setAttribute("renderRequest", renderRequest);
		renderRequest.setAttribute("renderResponse",  renderResponse);
		renderRequest.setAttribute("flashlightUtil",  new FlashlightUtil());
		renderRequest.setAttribute("structures", getWebContentStructures(scopeGroupId));
		renderRequest.setAttribute("searchAssetEntries", getAssetEntries());
		renderRequest.setAttribute("journalArticleLocalService", JournalArticleLocalServiceUtil.getService());
		
		
		
		super.render(renderRequest, renderResponse);
	}
	
	protected List<DDMStructure> getWebContentStructures(long groupid){
		List<DDMStructure> structures= new ArrayList<DDMStructure>();
		long classNameId = ClassNameLocalServiceUtil.getClassNameId(JournalArticle.class);
		try {
			long[] groupIds = PortalUtil.getCurrentAndAncestorSiteGroupIds(groupid, true);
		
			List<DDMStructure> groupstructures =  DDMStructureLocalServiceUtil.getStructures(groupIds);
			for(DDMStructure structure : groupstructures){
				if(structure.getClassNameId() == classNameId){
					structures.add(structure);
				}
			}
		} catch (PortalException e) {
			LOG.error(e);
		}
		return structures;
	}
	protected Map<String, String> getFacetsDefinitions(long groupid, long companyId) {

		Map<String, String> facets = new HashMap<String, String>();
		facets.put("BASIC-WEB-CONTENT", "Basic web content");
		List<DDMStructure> structures = DDMStructureLocalServiceUtil.getStructures(groupid);

		for (DDMStructure structure : structures) {
			String name = structure.getName("en_US");
			String key = structure.getStructureKey();

			try {
				GetterUtil.getLong(key);
				facets.put(key, name);
			} catch (Exception e) {

			}
		}

		List<DLFileEntryType> filetypes = DLFileEntryTypeLocalServiceUtil.getDLFileEntryTypes(0,
				DLFileEntryTypeLocalServiceUtil.getDLFileEntryTypesCount());

		for (DLFileEntryType filetype : filetypes) {
			String key = filetype.getFileEntryTypeId() + "";
			String name = filetype.getName("en_US");

			facets.put(key, name);
		}
		facets.put(BlogsEntry.class.getName(), "Blogs");

		return facets;
	}
	
	protected Map<String,String> getAssetEntries(){
		Map<String,String> assets = new HashMap<String,String>();
		
		assets.put(JournalArticle.class.getName(), "Articles (Web Content)");
		assets.put(DLFileEntry.class.getName(), "Documents & medias");
		assets.put(BlogsEntry.class.getName(), "Blogs");
		return assets;
	}
	

}