package com.savoirfairelinux.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.blogs.kernel.model.BlogsEntry;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortletRequestModel;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.util.bridges.freemarker.FreeMarkerPortlet;
import com.savoirfairelinux.portlet.searchdisplay.SearchDisplay;
import com.savoirfairelinux.portlet.searchdisplay.SearchResultWrapper;

@Component(
    immediate = true,
    service = Portlet.class,
    property = {
        "com.liferay.portlet.display-category=category.tools",
        "com.liferay.portlet.instanceable=false",
        "javax.portlet.display-name=Flashlight Search",
        "javax.portlet.init-param.template-path=/",
        "javax.portlet.init-param.view-template=/view.ftl",
        "javax.portlet.init-param.edit-template=/configuration.ftl",
        "javax.portlet.portlet-mode=text/html;view,edit",
        "javax.portlet.resource-bundle=content.Language",
        "javax.portlet.security-role-ref=power-user,user",
        "javax.portlet.name=" + SearchPortletKeys.NAME
    }
)
public class FlashlightSearchPortlet extends FreeMarkerPortlet {

    private static final Log LOG = LogFactoryUtil.getLog(FlashlightSearchPortlet.class);

    @Override
    public void render(RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {

        ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
        long scopeGroupId = themeDisplay.getScopeGroupId();
        String keywords = renderRequest.getParameter("keywords");

        SearchDisplay display = new SearchDisplay(renderRequest.getPreferences());
        Map<String, List<Document>> groupedDocuments = new HashMap<>();
        List<SearchResultWrapper> searchResults = new ArrayList<>();
        String[] enabled_facets = renderRequest.getPreferences().getValues("facets", new String[0]);
        if (keywords != null) {
            groupedDocuments = display.customGroupedSearch(renderRequest, keywords,
                    Field.ENTRY_CLASS_NAME);
            for(Entry<String, List<Document>> document : groupedDocuments.entrySet()){
                searchResults.add(new SearchResultWrapper(document.getKey(),document.getValue()));
            }
        }
        List<SearchFacet> searchFacets = display.getEnabledSearchFacets();
        Map<String, String> facets = getFacetsDefinitions(scopeGroupId, themeDisplay.getLocale());

        renderRequest.setAttribute("groupedDocuments", groupedDocuments);
        renderRequest.setAttribute("facets", facets);
        renderRequest.setAttribute("searchFacets", searchFacets);
        renderRequest.setAttribute("keywords", keywords);
        renderRequest.setAttribute("enabled_facets", enabled_facets);
        renderRequest.setAttribute("categories", AssetCategoryLocalServiceUtil.getCategories());
        renderRequest.setAttribute("searchResults", searchResults);
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

    @ProcessAction(name = "configurePortlet")
    public void configurePortletAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
        String[] displayStyle = actionRequest.getParameterValues("preferences--displayStyle--");
        String[] displayStyleGroupId = actionRequest.getParameterValues("preferences--displayStyleGroupId--");
        String[] facets = actionRequest.getParameterValues("selected_facets");
        String[] selectAssets = actionRequest.getParameterValues("selected_assets_entries");

        Enumeration<String> e = actionRequest.getParameterNames();
        while (e.hasMoreElements()) {
            String param = (String) e.nextElement();
            if (param.startsWith("ddm-")) {
                actionRequest.getPreferences().setValue(param, actionRequest.getParameter(param));
            }

        }
        if (facets == null) {
            facets = new String[0];
        }
        actionRequest.getPreferences().setValues("facets", facets);
        actionRequest.getPreferences().setValues("displayStyle", displayStyle);
        actionRequest.getPreferences().setValues("displayStyleGroupId", displayStyleGroupId);
        actionRequest.getPreferences().setValues("selectedAssets", selectAssets);
        actionRequest.getPreferences().store();

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
    protected Map<String, String> getFacetsDefinitions(long groupid, Locale locale) {

        Map<String, String> facets = new HashMap<String, String>();
        List<DDMStructure> structures = getWebContentStructures(groupid);
        for (DDMStructure structure : structures) {
            String name = structure.getName(locale);
            String key = structure.getStructureKey();
            facets.put(key, name);
        }

        List<DLFileEntryType> filetypes = DLFileEntryTypeLocalServiceUtil.getDLFileEntryTypes(0,
                DLFileEntryTypeLocalServiceUtil.getDLFileEntryTypesCount());

        for (DLFileEntryType filetype : filetypes) {
            String key = filetype.getFileEntryTypeId() + "";
            String name = filetype.getName(locale);
            facets.put(key, name);
        }
        facets.put(BlogsEntry.class.getName(), "Blogs");
        facets.put(User.class.getName(), "Users");
        facets.put(DLFolder.class.getName(), "Folders");

        return facets;
    }

    protected Map<String,String> getAssetEntries(){
        Map<String,String> assets = new HashMap<String,String>();

        assets.put(JournalArticle.class.getName(), "Articles (Web Content)");
        assets.put(DLFileEntry.class.getName(), "Documents & medias");
        assets.put(BlogsEntry.class.getName(), "Blogs");
        assets.put(User.class.getName(), "Users");
        assets.put(DLFolder.class.getName(), "Folders");

        return assets;
    }


}
