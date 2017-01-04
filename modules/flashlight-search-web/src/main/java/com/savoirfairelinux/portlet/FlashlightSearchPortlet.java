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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.util.bridges.freemarker.FreeMarkerPortlet;
import com.savoirfairelinux.configuration.SearchConfiguration;
import com.savoirfairelinux.portlet.searchdisplay.SearchDisplay;

import aQute.bnd.annotation.metatype.Configurable;

@Component(configurationPid = "com.savoirfairelinux.configuration.SearchConfiguration", immediate = true, property = {
		"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.instanceable=false",
		"javax.portlet.display-name=Flashlight Portlet", "javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.ftl",
		"javax.portlet.init-param.config-template=/configuration.jsp",
		"javax.portlet.init-param.edit-template=/configuration.ftl", "javax.portlet.portlet-mode=text/html;view,edit",
		"javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.name=" + SearchPortletKeys.NAME }, service = Portlet.class)
public class FlashlightSearchPortlet extends FreeMarkerPortlet {
	public FlashlightSearchPortlet() {

	}

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		long scopeGroupId = themeDisplay.getScopeGroupId();
		String keywords = renderRequest.getParameter("keywords");

		SearchDisplay display = new SearchDisplay(renderRequest.getPreferences());
		List<Document> documents = new ArrayList<Document>();
		Map<String, List<Document>> groupedDocuments = null;
		String[] enabled_facets= renderRequest.getPreferences().getValues("facets", new String[0]);
		/*enabled_facets.add(FreemarkerFileTypeFacet.class.getName());
		enabled_facets.add(FreemarkerStructureFacet.class.getName());*/
		
		if (keywords != null) {
			try {

				groupedDocuments = display.customGroupedSearch(renderRequest, keywords, renderRequest.getPreferences(),
						Field.ENTRY_CLASS_NAME);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		List<SearchFacet> searchFacets = display.getEnabledSearchFacets();
		

		renderRequest.setAttribute("searchFacets", searchFacets);

		Map<String, String> facets = getFacetsDefinitions(scopeGroupId, themeDisplay.getCompanyId());
		long[] defaulIds = new long[facets.size() + 1];
		String[] defaults = new String[defaulIds.length];
		for (int i = 0; i < defaulIds.length; i++) {
			defaulIds[i] = scopeGroupId;
			defaults[i] = "default";
		}
		String[] displayStyle = renderRequest.getPreferences().getValues("displayStyle", defaults);
		long[] displayStyleGroupId = GetterUtil
				.getLongValues(renderRequest.getPreferences().getValues("displayStyleGroupId", null), defaulIds);

		renderRequest.setAttribute("displayStyle", displayStyle);
		renderRequest.setAttribute("displayStyleGroupId", displayStyleGroupId);
		renderRequest.setAttribute("documentClassName", Document.class.getName());
		renderRequest.setAttribute("documents", documents);
		renderRequest.setAttribute("groupedDocuments", groupedDocuments);
		renderRequest.setAttribute("facets", facets);
		renderRequest.setAttribute("keywords", keywords);
		renderRequest.setAttribute("enabled_facets", enabled_facets);
		renderRequest.setAttribute("categories", AssetCategoryLocalServiceUtil.getCategories());
		
		

		super.render(renderRequest, renderResponse);
	}

	@Activate
	@Modified
	protected void activate(Map<Object, Object> properties) {
		searchConfiguration = Configurable.createConfigurable(SearchConfiguration.class, properties);
	}

	/*
	 * protected JournalArticleDisplay testArticleDisplay(RenderRequest
	 * renderRequest, RenderResponse renderResponse , long id){ JournalArticle
	 * article = null; JournalArticleDisplay articleDisplay =null; ThemeDisplay
	 * themeDisplay = (ThemeDisplay)
	 * renderRequest.getAttribute(WebKeys.THEME_DISPLAY); try { article =
	 * JournalArticleLocalServiceUtil.getArticle(id); articleDisplay =
	 * JournalArticleLocalServiceUtil.getArticleDisplay( article,
	 * article.getDDMTemplateKey(), "", themeDisplay.getLanguageId(), 1, new
	 * PortletRequestModel( renderRequest, renderResponse), themeDisplay); }
	 * catch (PortalException pe) {
	 * 
	 * } return articleDisplay; }
	 */

	protected Map<String, String> getFacetsDefinitions(long groupid, long companyId) {
		
		Map<String, String> facets = new HashMap<String, String>();
		facets.put("BASIC-WEB-CONTENT", "Basic web content");
		List<DDMStructure> structures = DDMStructureLocalServiceUtil.getStructures(groupid);

		for (DDMStructure structure : structures) {
			String name = structure.getName("en_US");
			String key = structure.getStructureKey();

			try {
				Long.parseLong(key);
				facets.put(key, name);
			} catch (Exception e) {

			}
		}

		List<DLFileEntryType> filetypes = DLFileEntryTypeLocalServiceUtil.getDLFileEntryTypes(0, 50);

		for (DLFileEntryType filetype : filetypes) {
			String key = filetype.getFileEntryTypeId() + "";
			String name = filetype.getName("en_US");

			facets.put(key, name);
		}

		return facets;
	}
	

	private Map<String, String> _facets;
	private volatile SearchConfiguration searchConfiguration;
}