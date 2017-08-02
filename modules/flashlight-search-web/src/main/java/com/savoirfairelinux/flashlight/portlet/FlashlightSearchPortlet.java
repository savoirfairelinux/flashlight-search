package com.savoirfairelinux.flashlight.portlet;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.template.TemplateManager;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.*;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portlet.display.template.PortletDisplayTemplate;
import com.savoirfairelinux.flashlight.portlet.framework.TemplatedPortlet;
import com.savoirfairelinux.flashlight.portlet.json.JSONPayloadFactory;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.model.SearchPage;
import com.savoirfairelinux.flashlight.service.model.SearchResultFacet;
import com.savoirfairelinux.flashlight.service.model.SearchResultsContainer;
import com.savoirfairelinux.flashlight.service.portlet.EditMode;
import com.savoirfairelinux.flashlight.service.portlet.FlashlightSearchPortletKeys;
import com.savoirfairelinux.flashlight.service.portlet.PortletRequestParameter;
import com.savoirfairelinux.flashlight.service.portlet.ViewMode;
import com.savoirfairelinux.flashlight.service.portlet.template.JournalArticleViewTemplateContextVariable;
import com.savoirfairelinux.flashlight.service.portlet.template.ViewContextVariable;
import com.savoirfairelinux.flashlight.service.util.PatternConstants;

import static java.lang.String.format;

@Component(
    service = Portlet.class,
    immediate = true,
    property = {
        "javax.portlet.name=" + FlashlightSearchPortletKeys.PORTLET_NAME,
        "javax.portlet.display-name=Flashlight Search",
        "javax.portlet.resource-bundle=content.Language",
        "javax.portlet.init-param.templates-path=/WEB-INF/",
        "javax.portlet.supports.mime-type=text/html",
        "javax.portlet.portlet-mode=text/html;view,edit,help",
        "javax.portlet.security-role-ref=power-user,user",

        "com.liferay.portlet.requires-namespaced-parameters=true",
        "com.liferay.portlet.display-category=category.tools",
        "com.liferay.portlet.instanceable=false",
        "com.liferay.portlet.header-portlet-javascript=" + FlashlightSearchPortlet.PATH_JAVASCRIPT
    }
)
public class FlashlightSearchPortlet extends TemplatedPortlet {

    private static final Log LOG = LogFactoryUtil.getLog(FlashlightSearchPortlet.class);

    public static final String PATH_JAVASCRIPT = "/javascript/flashlight.js";

    private static final String FORMAT_JAVASCRIPT_PATH = "%s%s";

    private static final Map<String, String> ASSET_TYPE_EDIT_VIEWS;
    static {
        HashMap<String, String> editViews = new HashMap<>(2);
        editViews.put(JournalArticle.class.getName(), "edit-tab-journal-article.ftl");
        editViews.put(DLFileEntry.class.getName(), "edit-tab-dl-file-entry.ftl");
        ASSET_TYPE_EDIT_VIEWS = Collections.unmodifiableMap(editViews);
    }

    private static final String ACTION_NAME_SAVE_TAB = "saveTab";
    private static final String ACTION_NAME_SAVE_GLOBAL = "saveGlobal";
    private static final String ACTION_NAME_SAVE_FACET_CONFIG = "saveFacetConfig";
    private static final String ACTION_NAME_DELETE_TAB = "deleteTab";

    private static final String FORM_FIELD_ADT_UUID = "adt-uuid";
    private static final String FORM_FIELD_DO_SEARCH_ON_STARTUP = "do-search-on-startup";
    private static final String FORM_FIELD_DO_SEARCH_ON_STARTUP_KEYWORDS = "do-search-on-startup-keywords";
    private static final String FORM_FIELD_TAB_ORDER = "tab-order";
    private static final String FORM_FIELD_PAGE_SIZE = "page-size";
    private static final String FORM_FIELD_FULL_PAGE_SIZE = "full-page-size";
    private static final String FORM_FIELD_LOAD_MORE_PAGE_SIZE = "load-more-page-size";
    private static final String FORM_FIELD_SEARCH_FACETS = "search-facets";
    private static final String FORM_FIELD_FACET_CLASS_NAME = "facet-class-name";
    private static final String FORM_FIELD_REDIRECT_URL = "redirect-url";
    private static final String FORM_FIELD_ASSET_TYPE = "asset-type";
    private static final String FORM_FIELD_JOURNAL_ARTICLE_VIEW_TEMPLATE = "journal-article-view-template";
    private static final String FORM_FIELD_SORT_BY = "sort-by";
    private static final List<String> FORM_FIELD_SORT_BY_AVAILABLE_FIELDS = Arrays.asList(
        "ratings",
        "createDate",
        "modified",
        "viewCount",
        "articleId",
        "publishDate",
        "priority",
        "title",
        "expirationDate",
        "displayDate"
    );
    private static final String FORM_FIELD_SORT_REVERSE = "sort-reverse";

    private static final Pattern FORM_FIELD_JOURNAL_TEMPLATE_UUID_PATTERN = Pattern.compile("^journal-article-template-" + PatternConstants.UUID + "$");
    private static final Pattern FORM_FIELD_DL_FILE_ENTRY_TYPE_TEMPLATE_UUID_PATTERN = Pattern.compile("^dl-file-entry-type-template-" + PatternConstants.UUID + "$");
    private static final Pattern FORM_FIELD_TITLE_PATTERN = Pattern.compile("^title-" + PatternConstants.LOCALE + "$");

    private static final Pattern PATTERN_CLASS_NAME = Pattern.compile("^" + PatternConstants.CLASS_NAME + "$");
    private static final Pattern PATTERN_UUID = Pattern.compile("^" + PatternConstants.UUID + "$");

    private static final String SESSION_MESSAGE_CONFIG_SAVED = "configuration.saved";

    private static final String ZERO = "0";
    private static final String ONE = "1";

    private static final String STATUS_CODE_NOT_FOUND = "404";
    private static final String STATUS_CODE_INTERNAL_ERROR = "500";

    private static final String CHARSET_JSON_UNICODE = "text/json;UTF-8";

    private static final String HTTP_HEADER_CACHE_CONTROL = "Cache-Control";
    private static final String CACHE_CONTROL_NO_CACHE = "no-cache";

    @Reference
    private JSONFactory jsonFactory;

    @Reference
    private Language language;

    @Reference
    private FlashlightSearchService searchService;

    @Reference
    private Portal portal;

    @Reference(target = "(language.type=" + TemplateConstants.LANG_TYPE_FTL + ")", unbind = "-")
    private TemplateManager templateManager;

    @Reference
    private AssetEntryLocalService assetEntryService;

    @Reference
    private DDMTemplateLocalService ddmTemplateLocalService;

    @Reference
    private PortletDisplayTemplate portletDisplayTemplate;

    /**
     * Routes between search results and single asset view
     *
     * @param request  The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException      If something goes wrong
     */
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String viewModeParam = ParamUtil.getString(request, PortletRequestParameter.VIEW_MODE.getName(), StringPool.BLANK);
        ViewMode viewMode = ViewMode.getViewMode(viewModeParam);
        switch(viewMode) {
            case RESULTS:
                this.doViewResults(request, response);
            break;
            case VIEW_JOURNAL:
                this.doViewJournal(request, response);
            break;
            default:
                this.doViewResults(request, response);
            break;
        }
    }

    /**
     * Displays the search view (both search fields and search results)
     *
     * @param request  The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException      If something goes wrong
     */
    public void doViewResults(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        HttpServletRequest httpServletRequest = this.portal.getHttpServletRequest(request);
        SearchContext searchContext = SearchContextFactory.getInstance(httpServletRequest);
        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        boolean doSearchOnStartup = config.doSearchOnStartup();
        String currentTabId = ParamUtil.get(request, PortletRequestParameter.TAB_ID.getName(), StringPool.BLANK);
        boolean performedSearch = false;
        String keywords = searchContext.getKeywords();

        if(Validator.isNull(keywords) && doSearchOnStartup) {
            keywords = config.getDoSearchOnStartupKeywords();
            searchContext.setKeywords(keywords);
        }

        if(tabs.size() == 1) {
            currentTabId = tabs.keySet().iterator().next();
        }

        if (!PATTERN_UUID.matcher(currentTabId).matches()) {
            currentTabId = null;
        }

        SearchResultsContainer results;
        if (Validator.isNotNull(keywords)) {
            try {
                results = this.searchService.search(request, response, currentTabId, 0, false);
                performedSearch = true;
            } catch (SearchException e) {
                throw new PortletException(e);
            }
        } else {
            results = new SearchResultsContainer(Collections.emptyMap());
        }

        HashMap<String, PortletURL> tabUrls = new HashMap<>(tabs.size());
        HashMap<String, ResourceURL> loadMoreUrls = new HashMap<>(tabs.size());

        for(Entry<String, FlashlightSearchConfigurationTab> tabEntry : tabs.entrySet()) {
            String tabUuid = tabEntry.getKey();
            FlashlightSearchConfigurationTab tab = tabEntry.getValue();


            // Tab URLs
            PortletURL tabUrl = response.createRenderURL();
            tabUrl.setParameter(PortletRequestParameter.TAB_ID.getName(), tabUuid);
            if(!keywords.isEmpty()) {
                tabUrl.setParameter(PortletRequestParameter.KEYWORDS.getName(), keywords);
            }
            tabUrls.put(tabUuid, tabUrl);

            // Put a load more URL only if we performed a search, that we have search results, and that the total search
            // results is more than a single page can contain
            if(performedSearch && results.hasSearchResults(tabUuid) && results.getSearchPage(tabUuid).getTotalSearchResults() > tab.getFullPageSize()) {
                ResourceURL loadMoreUrl = response.createResourceURL();
                loadMoreUrl.setResourceID(PortletResource.LOAD_MORE.getResourceId());
                loadMoreUrl.setParameter(PortletRequestParameter.TAB_ID.getName(), tabUuid);
                loadMoreUrl.setParameter(PortletRequestParameter.KEYWORDS.getName(), keywords);
                loadMoreUrl.setParameter(PortletRequestParameter.PAGE_OFFSET.getName(), ONE);
                loadMoreUrl.setParameter(PortletRequestParameter.RANDOM_CACHE.getName(), randomCacheParamValue());
                loadMoreUrls.put(tabUuid, loadMoreUrl);
            }
        }

        Map<String, Object> templateCtx = new HashMap<>(9);
        templateCtx.put(ViewContextVariable.NAMESPACE.getVariableName(), response.getNamespace());

        PortletURL keywordUrl = response.createRenderURL();
        keywordUrl.setParameter(PortletRequestParameter.KEYWORDS.getName(), keywords);

        String javascriptUrl = format(FORMAT_JAVASCRIPT_PATH, request.getContextPath(), PATH_JAVASCRIPT);

        templateCtx.put(ViewContextVariable.KEYWORD_URL.getVariableName(), keywordUrl);
        templateCtx.put(ViewContextVariable.TAB_URLS.getVariableName(), tabUrls);
        templateCtx.put(ViewContextVariable.LOAD_MORE_URLS.getVariableName(), loadMoreUrls);
        templateCtx.put(ViewContextVariable.RESULTS_CONTAINER.getVariableName(), results);
        templateCtx.put(ViewContextVariable.KEYWORDS.getVariableName(), keywords);
        templateCtx.put(ViewContextVariable.TAB_ID.getVariableName(), currentTabId);
        templateCtx.put(ViewContextVariable.FORMAT_FACET_TERM.getVariableName(), (BiFunction<SearchResultFacet, String, String>) (searchFacet, term) -> searchService.displayTerm(httpServletRequest, searchFacet, term));
        templateCtx.put(ViewContextVariable.JAVASCRIPT_PATH.getVariableName(), javascriptUrl);

        String adtUuid = config.getAdtUUID();
        if (adtUuid != null && PATTERN_UUID.matcher(adtUuid).matches()) {
            this.renderADT(request, response, templateCtx, adtUuid);
        } else {
            this.renderTemplate(request, response, templateCtx, "view.ftl");
        }
    }

    /**
     * Views a single journal article in the window
     *
     * @param request The request
     * @param response The response
     *
     * @throws PortletException
     * @throws IOException
     */
    public void doViewJournal(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();
        Class<JournalArticle> journalArticleClass = JournalArticle.class;
        Map<String, FlashlightSearchConfigurationTab> configTabs = this.searchService.readConfiguration(request.getPreferences()).getTabs();

        long classPK = ParamUtil.getLong(request, Field.ENTRY_CLASS_PK, 0l);
        String tabId = ParamUtil.getString(request, PortletRequestParameter.TAB_ID.getName(), StringPool.BLANK);
        String keywords = ParamUtil.getString(request, PortletRequestParameter.KEYWORDS.getName(), StringPool.BLANK);

        AssetRendererFactory<JournalArticle> factory = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClass(journalArticleClass);
        FlashlightSearchConfigurationTab configurationTab;
        AssetRenderer<JournalArticle> renderer;
        AssetEntry entry;

        if(PATTERN_UUID.matcher(tabId).matches() && configTabs.containsKey(tabId)) {
            configurationTab = configTabs.get(tabId);
        } else {
            throw new PortletException("No configuration tab found with ID \"" + tabId + "\"");
        }

        if(classPK > 0) {
            try {
                renderer = factory.getAssetRenderer(classPK);
                entry = this.assetEntryService.getEntry(journalArticleClass.getName(), classPK);
            } catch (PortalException e) {
                throw new PortletException("Cannot obtain asset renderer or asset entry", e);
            }
        } else {
            throw new PortletException("No asset sent");
        }

        try {
            if(renderer.hasViewPermission(permissionChecker)) {
                // We have an asset render, an asset entry and the permissions to render. Go!

                // Create the "back URL" to go to the search results
                PortletURL backUrl = response.createRenderURL();
                backUrl.setParameter(PortletRequestParameter.VIEW_MODE.getName(), ViewMode.RESULTS.getParamValue());
                backUrl.setParameter(PortletRequestParameter.TAB_ID.getName(), tabId);
                backUrl.setParameter(PortletRequestParameter.KEYWORDS.getName(), keywords);

                HashMap<String, Object> ctx = new HashMap<>(4);
                ctx.put(JournalArticleViewTemplateContextVariable.ASSET_RENDERER_FACTORY.getVariableName(), factory);
                ctx.put(JournalArticleViewTemplateContextVariable.ASSET_RENDERER.getVariableName(), renderer);
                ctx.put(JournalArticleViewTemplateContextVariable.ASSET_ENTRY.getVariableName(), entry);
                ctx.put(JournalArticleViewTemplateContextVariable.REDIRECT.getVariableName(), backUrl.toString());

                String adtUuid = configurationTab.getJournalArticleViewTemplate();
                if(adtUuid != null && PATTERN_UUID.matcher(adtUuid).matches()) {
                    this.renderADT(request, response, ctx, adtUuid);
                } else {
                    this.renderTemplate(request, response, ctx, "view-journal-article.ftl");
                }
            } else {
                throw new PortletException("Permission denied to Journal Article with classPK \"" + classPK + "\"");
            }
        } catch(PortalException e) {
            throw new PortletException("Cannot render article", e);
        }

    }

    /**
     * Routes between portlet resources
     *
     * @param request  The request
     * @param response The response
     *
     * @throws PortletException If something goes wrong
     * @throws IOException      If something goes wrong
     */
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        PortletResource resource = PortletResource.getResource(request);

        switch(resource) {
            case LOAD_MORE:
                this.doLoadMore(request, response);
            break;
            default:
                // Default to JSON response, send 404
                response.setCharacterEncoding(CHARSET_JSON_UNICODE);
                response.setProperty(ResourceResponse.HTTP_STATUS_CODE, STATUS_CODE_NOT_FOUND);
            break;
        }
    }

    /**
     * Performs the "load more" AJAX request
     *
     * @param request The resource request
     * @param response The resource response
     *
     * @throws PortletException If something goes wrong
     * @throws IOException If something goes wrong
     */
    public void doLoadMore(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        response.setContentType(CHARSET_JSON_UNICODE);
        response.setProperty(HTTP_HEADER_CACHE_CONTROL, CACHE_CONTROL_NO_CACHE);
        response.setProperty(ResourceResponse.EXPIRATION_CACHE, ZERO);

        String tabId = ParamUtil.get(request, PortletRequestParameter.TAB_ID.getName(), StringPool.BLANK);
        int offset = ParamUtil.getInteger(request, PortletRequestParameter.PAGE_OFFSET.getName(), 0);
        if(offset < 0) {
            offset = 0;
        }

        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();

        if(tabs.containsKey(tabId)) {
            try {
                SearchResultsContainer container = this.searchService.search(request, response, tabId, offset, true);
                JSONPayloadFactory jsonPayloadFactory = new JSONPayloadFactory(this.jsonFactory);
                if(container.hasSearchResults(tabId)) {
                    FlashlightSearchConfigurationTab tab = tabs.get(tabId);
                    SearchPage page = container.getSearchPage(tabId);

                    ResourceURL loadMoreUrl = response.createResourceURL();
                    loadMoreUrl.setResourceID(PortletResource.LOAD_MORE.getResourceId());
                    loadMoreUrl.setParameter(PortletRequestParameter.TAB_ID.getName(), tabId);
                    loadMoreUrl.setParameter(PortletRequestParameter.KEYWORDS.getName(), ParamUtil.get(request, PortletRequestParameter.KEYWORDS.getName(), StringPool.BLANK));
                    loadMoreUrl.setParameter(PortletRequestParameter.PAGE_OFFSET.getName(), Integer.toString(offset + 1));
                    loadMoreUrl.setParameter(PortletRequestParameter.RANDOM_CACHE.getName(), randomCacheParamValue());

                    JSONObject payload = jsonPayloadFactory.createJSONPayload(tab, page, offset, loadMoreUrl.toString());
                    response.getWriter().print(payload.toJSONString());
                } else {
                    // No search results with the given tab
                    LOG.debug("A search tab was found, but no pages were returned");
                    response.setProperty(ResourceResponse.HTTP_STATUS_CODE, STATUS_CODE_NOT_FOUND);
                }
            } catch (SearchException e) {
                // Cannot perform search, return a 500 error
                LOG.error("Cannot perform search", e);
                response.setProperty(ResourceResponse.HTTP_STATUS_CODE, STATUS_CODE_INTERNAL_ERROR);
            }
        } else {
            // No tab with given ID. Return 404
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE, STATUS_CODE_NOT_FOUND);
        }
    }

    /**
     * Routes between global configuration editing and tab editing
     *
     * @param request  The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException      If something goes wrong
     */
    @Override
    public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Very, very simple routing. That's all we need, folks.
        String editModeParam = ParamUtil.get(request, PortletRequestParameter.EDIT_MODE.getName(), StringPool.BLANK);
        EditMode editMode = EditMode.getEditMode(editModeParam);

        switch(editMode) {
            case TAB:
                this.doEditTab(request, response);
            break;
            case FACET:
                this.doEditFacet(request, response);
            break;
            default:
                this.doEditGlobal(request, response);
            break;
        }

    }

    /**
     * Render phase, edit mode, global configuration
     *
     * @param request  The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException      If something goes wrong
     */
    public void doEditGlobal(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();
        long groupId = themeDisplay.getScopeGroupId();
        Locale locale = themeDisplay.getLocale();

        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        String adtUuid = config.getAdtUUID();
        boolean doSearchOnStartup = config.doSearchOnStartup();
        String doSearchOnStartupKeywords = config.getDoSearchOnStartupKeywords();

        PortletURL editGlobalUrl = response.createRenderURL();
        editGlobalUrl.setPortletMode(PortletMode.EDIT);

        List<String> supportedAssetTypes = this.searchService.getSupportedAssetTypes();
        HashMap<String, PortletURL> createTabUrls = new HashMap<>(supportedAssetTypes.size());
        for(String assetType : supportedAssetTypes) {
            PortletURL createTabUrl = response.createRenderURL();
            createTabUrl.setPortletMode(PortletMode.EDIT);
            createTabUrl.setParameter(PortletRequestParameter.EDIT_MODE.getName(), EditMode.TAB.getParamValue());
            createTabUrl.setParameter(FORM_FIELD_ASSET_TYPE, assetType);
            createTabUrls.put(assetType, createTabUrl);
        }

        PortletURL saveGlobalUrl = response.createActionURL();
        saveGlobalUrl.setPortletMode(PortletMode.EDIT);
        saveGlobalUrl.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_SAVE_GLOBAL);

        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        HashMap<String, PortletURL> editTabUrls = new HashMap<>(tabs.size());
        HashMap<String, PortletURL> deleteTabUrls = new HashMap<>(tabs.size());
        tabs.keySet().forEach(tabId -> {
            PortletURL editUrl = response.createRenderURL();
            editUrl.setParameter(PortletRequestParameter.EDIT_MODE.getName(), EditMode.TAB.getParamValue());
            editUrl.setParameter(PortletRequestParameter.TAB_ID.getName(), tabId);
            editTabUrls.put(tabId, editUrl);

            PortletURL deleteUrl = response.createActionURL();
            deleteUrl.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_DELETE_TAB);
            deleteUrl.setParameter(PortletRequestParameter.TAB_ID.getName(), tabId);
            deleteUrl.setParameter(FORM_FIELD_REDIRECT_URL, editGlobalUrl.toString());
            deleteTabUrls.put(tabId, deleteUrl);
        });

        Map<String, List<DDMTemplate>> applicationDisplayTemplates;
        try {
            Map<Group, List<DDMTemplate>> applicationDisplayTemplatesByGroup = this.searchService.getPortletApplicationDisplayTemplates(permissionChecker, groupId);
            applicationDisplayTemplates = new LinkedHashMap<>(applicationDisplayTemplatesByGroup.size());
            for (Map.Entry<Group, List<DDMTemplate>> entry : applicationDisplayTemplatesByGroup.entrySet()) {
                applicationDisplayTemplates.put(entry.getKey().getName(locale), entry.getValue());
            }
        } catch (PortalException e) {
            throw new PortletException("Cannot fetch application display templates", e);
        }

        Map<String, List<DDMStructure>> availableStructures;
        try {
            availableStructures = this.searchService.getDDMStructures(groupId);
        } catch (PortalException e) {
            throw new PortletException("Unable to retrieve DDM structures", e);
        }

        Map<DLFileEntryType, List<DDMTemplate>> availableDlFileEntryTypeTemplates;

        try {
            availableDlFileEntryTypeTemplates = this.searchService.getFileEntryTypes(themeDisplay.getPermissionChecker(), groupId);
        } catch(PortalException e) {
            throw new PortletException("Unable to retrieve DLFileEntryType templates", e);
        }

        Map<String, List<DDMTemplate>> availableDlFileEntryTypeTemplatesUuidIndex = indexDlFileEntryTypeTemplatesByUuid(availableDlFileEntryTypeTemplates);

        HashMap<String, Object> templateCtx = new HashMap<>(14);
        templateCtx.put("ns", response.getNamespace());
        templateCtx.put("editGlobalUrl", editGlobalUrl);
        templateCtx.put("createTabUrls", createTabUrls);
        templateCtx.put("saveGlobalUrl", saveGlobalUrl);
        templateCtx.put("adtUuid", adtUuid);
        templateCtx.put("doSearchOnStartup", doSearchOnStartup);
        templateCtx.put("doSearchOnStartupKeywords", doSearchOnStartupKeywords);
        templateCtx.put("applicationDisplayTemplates", applicationDisplayTemplates);
        templateCtx.put("availableStructures", availableStructures);
        templateCtx.put("availableDlFileEntryTypeTemplates", availableDlFileEntryTypeTemplates);
        templateCtx.put("availableDlFileEntryTypeTemplatesUuidIndex", availableDlFileEntryTypeTemplatesUuidIndex);
        templateCtx.put("supportedAssetTypes", supportedAssetTypes);
        templateCtx.put("tabs", tabs);
        templateCtx.put("editTabUrls", editTabUrls);
        templateCtx.put("deleteTabUrls", deleteTabUrls);
        this.renderTemplate(request, response, templateCtx, "edit.ftl");
    }

    /**
     * Render phase, edit mode, tab configuration
     *
     * @param request  The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException      If something goes wrong
     */
    public void doEditTab(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();
        long scopeGroupId = themeDisplay.getScopeGroupId();

        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        String tabId = ParamUtil.get(request, PortletRequestParameter.TAB_ID.getName(), StringPool.BLANK);
        FlashlightSearchConfigurationTab tab;
        if (tabs.containsKey(tabId)) {
            tab = tabs.get(tabId);
        } else {
            tab = null;
            tabId = StringPool.BLANK;
        }

        int tabOrderRange = tabs.size() + 1;

        Set<Locale> availableLocales = this.language.getAvailableLocales(scopeGroupId);
        Map<String, List<DDMStructure>> availableJournalArticleStructures;
        try {
            availableJournalArticleStructures = this.searchService.getDDMStructures(scopeGroupId);
        } catch (PortalException e) {
            throw new PortletException("Unable to retrieve DDM structures", e);
        }

        Map<Group, List<DDMTemplate>> availableJournalArticleViewTemplates;
        try {
            availableJournalArticleViewTemplates = this.searchService.getJournalArticleViewTemplates(permissionChecker, scopeGroupId);
        } catch (PortalException e) {
            throw new PortletException("Unable to retreive Journal Article search result ADTs", e);
        }

        Map<DLFileEntryType, List<DDMTemplate>> availableDlFileEntryTemplates;
        try {
            availableDlFileEntryTemplates = this.searchService.getFileEntryTypes(permissionChecker, scopeGroupId);
        } catch (PortalException e) {
            throw new PortletException("Unable to retrieve DL File entry types", e);
        }

        // Index DDM templates by UUID for FreeMarker
        Map<String, List<DDMTemplate>> availableDlFileEntryTemplatesUuuidIndex = indexDlFileEntryTypeTemplatesByUuid(availableDlFileEntryTemplates);

        PortletURL editGlobalURL = response.createRenderURL();
        editGlobalURL.setPortletMode(PortletMode.EDIT);

        PortletURL saveTabURL = response.createActionURL();
        saveTabURL.setPortletMode(PortletMode.EDIT);
        saveTabURL.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_SAVE_TAB);

        int tabOrder;
        int tabPageSize;
        int tabFullPageSize;
        int tabLoadMorePageSize;
        String assetType;
        String journalArticleViewTemplate;
        String sortBy;
        boolean sortReverse;
        Map<String, String> searchFacets;
        Map<String, PortletURL> searchFacetsUrls;
        Map<String, String> journalArticleTemplates;
        Map<String, String> dlFileEntryTypeTemplates;
        Map<String, String> titleMap;
        PortletURL redirectUrl = response.createRenderURL();

        if (tab != null) {
            tabOrder = tab.getOrder();
            tabPageSize = tab.getPageSize();
            tabFullPageSize = tab.getFullPageSize();
            tabLoadMorePageSize = tab.getLoadMorePageSize();
            assetType = tab.getAssetType();
            journalArticleViewTemplate = tab.getJournalArticleViewTemplate();
            searchFacets = tab.getSearchFacets();
            sortBy = tab.getSortBy();
            sortReverse = tab.isSortReverse();

            searchFacetsUrls = new HashMap<>(searchFacets.size());
            for(String facetClassName : searchFacets.keySet()) {
                PortletURL facetUrl = response.createRenderURL();
                facetUrl.setParameter(PortletRequestParameter.EDIT_MODE.getName(), EditMode.FACET.getParamValue());
                facetUrl.setParameter(PortletRequestParameter.TAB_ID.getName(), tabId);
                facetUrl.setParameter(FORM_FIELD_FACET_CLASS_NAME, facetClassName);
                searchFacetsUrls.put(facetClassName, facetUrl);
            }

            journalArticleTemplates = tab.getJournalArticleTemplates();
            dlFileEntryTypeTemplates = tab.getDLFileEntryTypeTemplates();
            titleMap = tab.getTitleMap();
            redirectUrl.setParameter(PortletRequestParameter.EDIT_MODE.getName(), EditMode.TAB.getParamValue());
            redirectUrl.setParameter(PortletRequestParameter.TAB_ID.getName(), tabId);
        } else {
            tabOrder = tabOrderRange;
            tabPageSize = FlashlightSearchConfigurationTab.DEFAULT_PAGE_SIZE;
            tabFullPageSize = FlashlightSearchConfigurationTab.DEFAULT_FULL_PAGE_SIZE;
            tabLoadMorePageSize = FlashlightSearchConfigurationTab.DEFAULT_LOAD_MORE_PAGE_SIZE;
            assetType = ParamUtil.get(request, FORM_FIELD_ASSET_TYPE, StringPool.BLANK);
            if(!PATTERN_CLASS_NAME.matcher(assetType).matches()) {
                assetType = StringPool.BLANK;
            }
            journalArticleViewTemplate = ParamUtil.get(request, FORM_FIELD_JOURNAL_ARTICLE_VIEW_TEMPLATE, StringPool.BLANK);
            if(!PATTERN_UUID.matcher(journalArticleViewTemplate).matches()) {
                journalArticleViewTemplate = StringPool.BLANK;
            }
            searchFacets = Collections.emptyMap();
            sortBy = StringPool.BLANK;
            sortReverse = false;
            searchFacetsUrls = Collections.emptyMap();
            journalArticleTemplates = Collections.emptyMap();
            dlFileEntryTypeTemplates = Collections.emptyMap();
            titleMap = Collections.emptyMap();
        }

        List<String> supportedAssetTypes = this.searchService.getSupportedAssetTypes();
        List<SearchFacet> supportedSearchFacets = this.searchService.getSupportedSearchFacets();

        HashMap<String, Object> templateCtx = new HashMap<>(24);
        // Base template data
        templateCtx.put("ns", response.getNamespace());
        templateCtx.put("editGlobalUrl", editGlobalURL.toString());
        templateCtx.put("saveTabUrl", saveTabURL.toString());
        templateCtx.put("redirectUrl", redirectUrl);

        // Configuration data
        templateCtx.put("tabId", tabId);
        templateCtx.put("tabOrder", tabOrder);
        templateCtx.put("tabPageSize", tabPageSize);
        templateCtx.put("tabFullPageSize", tabFullPageSize);
        templateCtx.put("tabLoadMorePageSize", tabLoadMorePageSize);
        templateCtx.put("tabOrderRange", tabOrderRange);
        templateCtx.put("availableLocales", availableLocales);
        templateCtx.put("availableJournalArticleStructures", availableJournalArticleStructures);
        templateCtx.put("availableDlFileEntryTypeTemplates", availableDlFileEntryTemplates);
        templateCtx.put("availableDlFileEntryTypeTemplatesUuidIndex", availableDlFileEntryTemplatesUuuidIndex);
        templateCtx.put("supportedAssetTypes", supportedAssetTypes);
        templateCtx.put("availableJournalArticleViewTemplates", availableJournalArticleViewTemplates);
        templateCtx.put("supportedSearchFacets", supportedSearchFacets);
        templateCtx.put("assetType", assetType);
        templateCtx.put("journalArticleViewTemplate", journalArticleViewTemplate);
        templateCtx.put("assetTypeEditViews", ASSET_TYPE_EDIT_VIEWS);
        templateCtx.put("searchFacets", searchFacets);
        templateCtx.put("searchFacetUrls", searchFacetsUrls);
        templateCtx.put("journalArticleTemplates", journalArticleTemplates);
        templateCtx.put("dlFileEntryTypeTemplates", dlFileEntryTypeTemplates);
        templateCtx.put("titleMap", titleMap);
        templateCtx.put("sortByAvailableFields", FORM_FIELD_SORT_BY_AVAILABLE_FIELDS);
        templateCtx.put("sortBy", sortBy);
        templateCtx.put("sortReverse", sortReverse);
        this.renderTemplate(request, response, templateCtx, "edit-tab.ftl");
    }

    /**
     * Edits the facet configuration for a given tab
     *
     * @param request The request
     * @param response The response
     *
     * @throws IOException      If something goes wrong
     * @throws PortletException If something goes wrong
     */
    public void doEditFacet(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String tabId = ParamUtil.getString(request, PortletRequestParameter.TAB_ID.getName(), StringPool.BLANK);
        String facetClassName = ParamUtil.getString(request, FORM_FIELD_FACET_CLASS_NAME, StringPool.BLANK);
        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        SearchFacet targetFacet = this.getSearchFacetFromRequest(tabId, facetClassName, config);

        // If we have a valid facet, show its configuration
        if(targetFacet != null) {
            // This is needed by Liferay facets JSP configs
            HttpServletRequest servletRequest = this.portal.getHttpServletRequest(request);
            HttpServletResponse servletResponse = this.portal.getHttpServletResponse(response);
            servletRequest.setAttribute("facet_configuration.jsp-searchFacet", targetFacet);
            servletRequest.setAttribute("search.jsp-facet", targetFacet.getFacet());

            try {
                String facetConfig = config.getTabs().get(tabId).getSearchFacets().get(facetClassName);

                if(!facetConfig.isEmpty()) {
                    targetFacet.getFacetConfiguration().setDataJSONObject(this.jsonFactory.createJSONObject(facetConfig));
                }
            } catch(Exception e) {
                LOG.error("Unable to reinitialize facet with stored configuration - resetting", e);
            }

            // Now we assemble our own view that includes the JSP configuration
            HashMap<String, Object> templateCtx = new HashMap<>(8);

            PortletURL editTabUrl = response.createRenderURL();
            editTabUrl.setParameter(PortletRequestParameter.EDIT_MODE.getName(), EditMode.TAB.getParamValue());
            editTabUrl.setParameter(PortletRequestParameter.TAB_ID.getName(), tabId);

            PortletURL saveFacetConfigUrl = response.createActionURL();
            saveFacetConfigUrl.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_SAVE_FACET_CONFIG);

            templateCtx.put("ns", response.getNamespace());
            templateCtx.put("editTabUrl", editTabUrl);
            templateCtx.put("redirectUrl", editTabUrl);
            templateCtx.put("saveFacetConfigUrl", saveFacetConfigUrl);
            templateCtx.put("tabId", tabId);
            templateCtx.put("searchFacet", targetFacet);
            templateCtx.put("servletRequest", servletRequest);
            templateCtx.put("servletResponse", servletResponse);


            this.renderTemplate(request, response, templateCtx, "edit-facet.ftl");
        }
    }

    /**
     * Saves the global aspect of the configuration
     *
     * @param request  The request
     * @param response The response
     * @throws IOException      If something goes wrong
     * @throws PortletException If something goes wrong
     */
    @ProcessAction(name = ACTION_NAME_SAVE_GLOBAL)
    public void actionSaveGlobal(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        String redirectUrl = ParamUtil.get(request, FORM_FIELD_REDIRECT_URL, StringPool.BLANK);
        String adtUuid = ParamUtil.get(request, FORM_FIELD_ADT_UUID, StringPool.BLANK);
        boolean doSearchOnStartup = ParamUtil.getBoolean(request, FORM_FIELD_DO_SEARCH_ON_STARTUP, false);

        if (!PATTERN_UUID.matcher(adtUuid).matches()) {
            adtUuid = StringPool.BLANK;
        }

        String doSearchOnStartupKeywords = ParamUtil.getString(request, FORM_FIELD_DO_SEARCH_ON_STARTUP_KEYWORDS, FlashlightSearchService.CONFIGURATION_DEFAULT_SEARCH_KEYWORDS);
        this.searchService.saveGlobalSettings(adtUuid, doSearchOnStartup, doSearchOnStartupKeywords, request.getPreferences());

        SessionMessages.add(request, SESSION_MESSAGE_CONFIG_SAVED);
        if (!redirectUrl.isEmpty()) {
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Saves a Flashlight Search configuration tab as portlet preferences.
     * <p>
     * The save action performs very basic validation - it checks for format validity, but it will not check whether
     * objects referenced by the parameters exist. We are not holding the user's hands on this one - the form consists
     * of select fields generated by Liferay data. If the user tampers with such data in a way that breaks the format,
     * such data will not be saved. No error will be displayed in this case.
     *
     * @param request  The request
     * @param response The response
     * @throws IOException      If configuration fails to save
     * @throws PortletException If a portlet error occurs
     */
    @ProcessAction(name = ACTION_NAME_SAVE_TAB)
    public void actionSaveTab(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        // Get raw parameters
        String redirectUrl = ParamUtil.get(request, FORM_FIELD_REDIRECT_URL, StringPool.BLANK);
        String tabId = ParamUtil.get(request, PortletRequestParameter.TAB_ID.getName(), StringPool.BLANK);
        String tabOrder = ParamUtil.get(request, FORM_FIELD_TAB_ORDER, Integer.toString(FlashlightSearchConfigurationTab.DEFAULT_ORDER));
        String pageSize = ParamUtil.get(request, FORM_FIELD_PAGE_SIZE, Integer.toString(FlashlightSearchConfigurationTab.DEFAULT_PAGE_SIZE));
        String fullPageSize = ParamUtil.get(request, FORM_FIELD_FULL_PAGE_SIZE, Integer.toString(FlashlightSearchConfigurationTab.DEFAULT_FULL_PAGE_SIZE));
        String loadMorePageSize = ParamUtil.get(request, FORM_FIELD_LOAD_MORE_PAGE_SIZE, Integer.toString(FlashlightSearchConfigurationTab.DEFAULT_LOAD_MORE_PAGE_SIZE));
        String[] selectedFacets = ParamUtil.getParameterValues(request, FORM_FIELD_SEARCH_FACETS, StringPool.EMPTY_ARRAY);
        String selectedAssetType = ParamUtil.get(request, FORM_FIELD_ASSET_TYPE, StringPool.BLANK);
        String selectedJournalArticleViewTemplate = ParamUtil.get(request, FORM_FIELD_JOURNAL_ARTICLE_VIEW_TEMPLATE, StringPool.BLANK);
        String sortBy = ParamUtil.get(request, FORM_FIELD_SORT_BY, StringPool.BLANK);
        String sortReverse = ParamUtil.get(request, FORM_FIELD_SORT_REVERSE, StringPool.FALSE);

        HashMap<String, String> validatedJournalArticleTemplates = new HashMap<>();
        HashMap<String, String> validatedDlFileEntryTemplates = new HashMap<>();
        HashMap<String, String> validatedTitleMap = new HashMap<>();

        // DDM Template UUIDs and locales are validated on the fly
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = ParamUtil.get(request, paramName, StringPool.BLANK);
            Matcher journalTemplateMatcher = FORM_FIELD_JOURNAL_TEMPLATE_UUID_PATTERN.matcher(paramName);
            Matcher dlFileEntryTemplateMatcher = FORM_FIELD_DL_FILE_ENTRY_TYPE_TEMPLATE_UUID_PATTERN.matcher(paramName);
            Matcher titleMatcher = FORM_FIELD_TITLE_PATTERN.matcher(paramName);

            if (journalTemplateMatcher.matches()) {
                if (PATTERN_UUID.matcher(paramValue).matches()) {
                    validatedJournalArticleTemplates.put(journalTemplateMatcher.group(1), paramValue);
                }
            } else if (dlFileEntryTemplateMatcher.matches()) {
                if(PATTERN_UUID.matcher(paramValue).matches()) {
                    validatedDlFileEntryTemplates.put(dlFileEntryTemplateMatcher.group(1), paramValue);
                }
            } else if (titleMatcher.matches()) {
                // Escape the title
                validatedTitleMap.put(titleMatcher.group(1), HtmlUtil.escape(paramValue));
            }
        }

        // Validate the fields
        String validatedTabId;
        if (PATTERN_UUID.matcher(tabId).matches()) {
            validatedTabId = tabId;
        } else {
            validatedTabId = null;
        }

        int validatedTabOrder;
        try {
            validatedTabOrder = Integer.parseInt(tabOrder);
        } catch (NumberFormatException e) {
            validatedTabOrder = 0;
        }

        int validatedPageSize;
        try {
            validatedPageSize = Integer.parseInt(pageSize);
        } catch (NumberFormatException e) {
            validatedPageSize = FlashlightSearchConfigurationTab.DEFAULT_PAGE_SIZE;
        }

        int validatedFullPageSize;
        try {
            validatedFullPageSize = Integer.parseInt(fullPageSize);
        } catch (NumberFormatException e) {
            validatedFullPageSize = FlashlightSearchConfigurationTab.DEFAULT_FULL_PAGE_SIZE;
        }

        int validatedLoadMorePageSize;
        try {
            validatedLoadMorePageSize = Integer.parseInt(loadMorePageSize);
        } catch(NumberFormatException e) {
            validatedLoadMorePageSize = FlashlightSearchConfigurationTab.DEFAULT_LOAD_MORE_PAGE_SIZE;
        }

        String validatedSortBy = StringPool.BLANK;
        if (FORM_FIELD_SORT_BY_AVAILABLE_FIELDS.contains(sortBy)) {
            validatedSortBy = sortBy;
        }

        boolean validatedSortReverse = ("on".equals(sortReverse));

        List<SearchFacet> supportedFacets = this.searchService.getSupportedSearchFacets();
        HashMap<String, String> validatedSelectedFacets = new HashMap<>(selectedFacets.length);
        for (String facet : selectedFacets) {
            if (PATTERN_CLASS_NAME.matcher(facet).matches()) {
                SearchFacet correspondingSearchFacet = supportedFacets
                    .stream()
                    .filter(sf -> sf.getClass().getName().equals(facet))
                    .findFirst()
                    .orElse(null);
                if(correspondingSearchFacet != null) {
                    validatedSelectedFacets.put(facet, StringPool.BLANK);
                }
            }
        }

        String validatedAssetType;
        if (PATTERN_CLASS_NAME.matcher(selectedAssetType).matches()) {
            validatedAssetType = selectedAssetType;
        } else {
            validatedAssetType = StringPool.BLANK;
        }

        String validatedJournalArticleViewTemplate;
        if(PATTERN_UUID.matcher(selectedJournalArticleViewTemplate).matches()) {
            validatedJournalArticleViewTemplate = selectedJournalArticleViewTemplate;
        } else {
            validatedJournalArticleViewTemplate = StringPool.BLANK;
        }

        // Create or save the configuration tab and store it
        FlashlightSearchConfigurationTab tab;
        if (validatedTabId != null) {
            tab = new FlashlightSearchConfigurationTab(validatedTabId, validatedTabOrder, validatedPageSize, validatedFullPageSize, validatedLoadMorePageSize, validatedTitleMap, validatedAssetType, validatedJournalArticleViewTemplate, validatedSelectedFacets, validatedJournalArticleTemplates, validatedDlFileEntryTemplates, validatedSortBy, validatedSortReverse);
        } else {
            tab = new FlashlightSearchConfigurationTab(validatedTabOrder, validatedPageSize, validatedFullPageSize, validatedLoadMorePageSize, validatedTitleMap, validatedAssetType, validatedJournalArticleViewTemplate, validatedSelectedFacets, validatedJournalArticleTemplates, validatedDlFileEntryTemplates, validatedSortBy, validatedSortReverse);
        }
        this.searchService.saveConfigurationTab(tab, request.getPreferences());

        SessionMessages.add(request, SESSION_MESSAGE_CONFIG_SAVED);

        if (!redirectUrl.isEmpty()) {
            response.sendRedirect(redirectUrl);
        }
    }

    @ProcessAction(name = ACTION_NAME_SAVE_FACET_CONFIG)
    public void actionSaveFacetConfig(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String tabId = ParamUtil.get(request, PortletRequestParameter.TAB_ID.getName(), StringPool.BLANK);
        String facetClassName = ParamUtil.get(request, FORM_FIELD_FACET_CLASS_NAME, StringPool.BLANK);
        String redirectUrl = ParamUtil.get(request, FORM_FIELD_REDIRECT_URL, StringPool.BLANK);
        PortletPreferences preferences = request.getPreferences();
        FlashlightSearchConfiguration configuration = this.searchService.readConfiguration(preferences);
        SearchFacet targetFacet = this.getSearchFacetFromRequest(tabId, facetClassName, configuration);

        if(targetFacet != null) {
            JSONObject facetConfiguration = targetFacet.getJSONData(request);
            targetFacet.getFacetConfiguration().setDataJSONObject(facetConfiguration);
            this.searchService.saveSearchFacetConfig(configuration.getTabs().get(tabId), targetFacet, preferences);
            SessionMessages.add(request, SESSION_MESSAGE_CONFIG_SAVED);
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Deletes a tab from the configuration
     *
     * @param request  The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException      If something goes wrong
     */
    @ProcessAction(name = ACTION_NAME_DELETE_TAB)
    public void actionDeleteTab(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String tabId = ParamUtil.get(request, PortletRequestParameter.TAB_ID.getName(), StringPool.BLANK);
        String redirectUrl = ParamUtil.get(request, FORM_FIELD_REDIRECT_URL, StringPool.BLANK);

        if (tabId != null && PATTERN_UUID.matcher(tabId).matches()) {
            PortletPreferences preferences = request.getPreferences();
            Map<String, FlashlightSearchConfigurationTab> tabs = this.searchService.readConfiguration(preferences).getTabs();
            if (tabs.containsKey(tabId)) {
                this.searchService.deleteConfigurationTab(tabId, preferences);
            }
        }

        SessionMessages.add(request, SESSION_MESSAGE_CONFIG_SAVED);

        if (!redirectUrl.isEmpty()) {
            response.sendRedirect(redirectUrl);
        }
    }


    /**
     * <p>Returns a tab's search facet object extracted from the request's information. The following is done:</p>
     * <ul>
     *   <li>The given tab ID is validated and checked for existence in the configuration</li>
     *   <li>The given facet class name is validated and checked for existence in the tab</li>
     *   <li>The search facet is obtained from the list of supported facets</li>
     * </ul>
     * <p>When all of these conditions are met, a facet object is returned.</p>
     *
     * @param tabId The tab's ID
     * @param facetClassName The facet's class name
     * @param configuration The search portlet configuration
     * @return The facet object or null if the conditions are not met
     */
    private SearchFacet getSearchFacetFromRequest(String tabId, String facetClassName, FlashlightSearchConfiguration configuration) {
        Map<String, FlashlightSearchConfigurationTab> tabs = configuration.getTabs();
        SearchFacet returnedFacet;

        // Valid tab ID, tab ID exists, valid facet class name, facet class name in tab config
        if(
                PATTERN_UUID.matcher(tabId).matches() &&
                tabs.containsKey(tabId) &&
                PATTERN_CLASS_NAME.matcher(facetClassName).matches() &&
                tabs.get(tabId).getSearchFacets().containsKey(facetClassName)) {

            FlashlightSearchConfigurationTab targetTab = tabs.get(tabId);
            SearchFacet supportedFacet = this.searchService.getSupportedSearchFacets()
                .stream()
                .filter(f -> f.getClass().getName().equals(facetClassName))
                .findFirst()
                .orElse(null);

            if(targetTab != null && supportedFacet != null) {
                returnedFacet = supportedFacet;
            } else {
                returnedFacet = null;
            }
        } else {
            returnedFacet = null;
        }

        return returnedFacet;
    }

    @Override
    protected Portal getPortal() {
        return this.portal;
    }

    @Override
    protected TemplateManager getTemplateManager() {
        return this.templateManager;
    }

    @Override
    protected PortletDisplayTemplate getPortletDisplayTemplate() {
        return this.portletDisplayTemplate;
    }

    @Override
    protected DDMTemplateLocalService getDDMTemplateLocalService() {
        return this.ddmTemplateLocalService;
    }

    /**
     * Indexes the ddm templates by file entry type UUID
     * @param fileEntryTemplates The ddm templates
     * @return The re-indexed ddm templates, by UUID
     */
    private static final Map<String, List<DDMTemplate>> indexDlFileEntryTypeTemplatesByUuid(Map<DLFileEntryType, List<DDMTemplate>> fileEntryTemplates) {
        HashMap<String, List<DDMTemplate>> index = new HashMap<>(fileEntryTemplates.size());
        for(Entry<DLFileEntryType, List<DDMTemplate>> entry : fileEntryTemplates.entrySet()) {
            index.put(entry.getKey().getUuid(), entry.getValue());
        }
        return index;
    }

    /**
     * @return Returns a random number between 0 and 1000, used to prevent caching of XHR requests
     */
    private static final String randomCacheParamValue() {
        return Integer.toString(ThreadLocalRandom.current().nextInt(1001));
    }
}
