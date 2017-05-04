package com.savoirfairelinux.flashlight.portlet;

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
import javax.portlet.PortletPreferences;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.blogs.kernel.model.BlogsEntry;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.template.TemplateManager;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.savoirfairelinux.flashlight.portlet.framework.TemplatedPortlet;
import com.savoirfairelinux.flashlight.service.SearchService;
import com.savoirfairelinux.flashlight.service.model.SearchResultWrapper;

@Component(
    service = Portlet.class,
    immediate = true,
    property = {
        "com.liferay.portlet.instanceable=false",
        "com.liferay.portlet.display-category=category.tools",
        "javax.portlet.name=" + SearchService.PORTLET_NAME,
        "javax.portlet.display-name=Flashlight Search",
        "javax.portlet.resource-bundle=content.Language",
        "javax.portlet.init-param.templates-path=/",
        "javax.portlet.supports.mime-type=text/html",
        "javax.portlet.portlet-mode=text/html;view,edit",
        "javax.portlet.security-role-ref=power-user,user",
    }
)
public class FlashlightSearchPortlet extends TemplatedPortlet {

    private static final Log LOG = LogFactoryUtil.getLog(FlashlightSearchPortlet.class);

    private AssetCategoryLocalService assetCategoryLocalService;
    private DDMStructureLocalService DDMStructureLocalService;
    private DLFileEntryTypeLocalService DLFileEntryTypeLocalService;
    private ClassNameLocalService classNameLocalService;
    private SearchService searchService;

    private Portal portal;
    private TemplateManager templateManager;

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        PortletPreferences preferences = request.getPreferences();
        HttpServletRequest servletRequest = this.portal.getHttpServletRequest(request);
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        long scopeGroupId = themeDisplay.getScopeGroupId();
        String keywords = request.getParameter("keywords");

        Map<String, List<Document>> groupedDocuments = new HashMap<>();
        List<SearchResultWrapper> searchResults = new ArrayList<>();
        String[] enabled_facets = request.getPreferences().getValues("facets", new String[0]);
        if (keywords != null) {

            SearchContext searchContext = SearchContextFactory.getInstance(servletRequest);
            searchContext.setKeywords(keywords);

            groupedDocuments = this.searchService.customGroupedSearch(searchContext, request.getPreferences(), Field.ENTRY_CLASS_NAME, 800);
            for(Entry<String, List<Document>> document : groupedDocuments.entrySet()){
                searchResults.add(new SearchResultWrapper(document.getKey(),document.getValue()));
            }
        }
        List<SearchFacet> searchFacets = this.searchService.getEnabledSearchFacets(preferences);
        Map<String, String> facets = getFacetsDefinitions(scopeGroupId, themeDisplay.getLocale());

        HashMap<String, Object> templateCtx = new HashMap<>();
        templateCtx.put("groupedDocuments", groupedDocuments);
        templateCtx.put("facets", facets);
        templateCtx.put("searchFacets", searchFacets);
        templateCtx.put("keywords", keywords);
        templateCtx.put("enabled_facets", enabled_facets);
        templateCtx.put("categories", assetCategoryLocalService.getCategories());
        templateCtx.put("searchResults", searchResults);
        templateCtx.put("flashlightUtil",  new FlashlightUtil());
        this.renderTemplate(request, response, templateCtx, "view.ftl");
    }

    @Override
    protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        HashMap<String, Object> templateCtx = new HashMap<>();
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        long scopeGroupId = themeDisplay.getScopeGroupId();
        PortletPreferences preferences = request.getPreferences();

        List<SearchFacet> searchFacets = this.searchService.getEnabledSearchFacets(preferences);
        String[] enabled_facets = request.getPreferences().getValues("facets", new String[0]);

        templateCtx.put("enabled_facets", enabled_facets);
        templateCtx.put("searchAssetEntries", getAssetEntries());
        templateCtx.put("searchFacets", searchFacets);
        templateCtx.put("structures", getWebContentStructures(scopeGroupId));
        this.renderTemplate(request, response, templateCtx, "configuration.ftl");
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
        long classNameId = classNameLocalService.getClassNameId(JournalArticle.class);
        try {
            long[] groupIds = PortalUtil.getCurrentAndAncestorSiteGroupIds(groupid, true);

            List<DDMStructure> groupstructures =  DDMStructureLocalService.getStructures(groupIds);
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

        List<DLFileEntryType> filetypes = DLFileEntryTypeLocalService.getDLFileEntryTypes(0,
                DLFileEntryTypeLocalService.getDLFileEntryTypesCount());

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

    @Reference(unbind = "-")
    public void setAssetCategoryLocalService(AssetCategoryLocalService assetCategoryLocalService){
        this.assetCategoryLocalService  = assetCategoryLocalService;
    }

    @Reference(unbind = "-")
    public void setDDMStructureLocalService(DDMStructureLocalService DDMStructureLocalService){
        this.DDMStructureLocalService  = DDMStructureLocalService;
    }
    @Reference(unbind = "-")
    public void setDLFileEntryTypeLocalService(DLFileEntryTypeLocalService dLFileEntryTypeLocalService){
        this.DLFileEntryTypeLocalService  = dLFileEntryTypeLocalService;
    }

    @Reference(unbind = "-")
    public void setClassNameLocalService(ClassNameLocalService classNameLocalService){
        this.classNameLocalService  = classNameLocalService;
    }

    @Reference(unbind = "-")
    public void setSearchService(SearchService service) {
        this.searchService = service;
    }

    @Reference
    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    @Override
    protected Portal getPortal() {
        return this.portal;
    }

    @Reference(target = "(language.type=" + TemplateConstants.LANG_TYPE_FTL + ")")
    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    protected TemplateManager getTemplateManager() {
        return this.templateManager;
    }

}
