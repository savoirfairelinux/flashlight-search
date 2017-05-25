package com.savoirfairelinux.flashlight.portlet;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.portlet.*;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
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
import com.savoirfairelinux.flashlight.portlet.template.ViewContextVariable;
import com.savoirfairelinux.flashlight.service.FlashlightSearchPortletKeys;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.model.SearchPage;
import com.savoirfairelinux.flashlight.service.model.SearchResultsContainer;
import com.savoirfairelinux.flashlight.service.util.PatternConstants;

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
        "com.liferay.portlet.header-portlet-javascript=/javascript/flashlight.js"
    }
)
public class FlashlightSearchPortlet extends TemplatedPortlet {

    private static final Log LOG = LogFactoryUtil.getLog(FlashlightSearchPortlet.class);

    private static final String ACTION_NAME_SAVE_TAB = "saveTab";
    private static final String ACTION_NAME_SAVE_GLOBAL = "saveGlobal";
    private static final String ACTION_NAME_SAVE_FACET_CONFIG = "saveFacetConfig";
    private static final String ACTION_NAME_DELETE_TAB = "deleteTab";

    private static final String FORM_FIELD_KEYWORDS = "keywords";
    private static final String FORM_FIELD_EDIT_MODE = "edit-mode";
    private static final String FORM_FIELD_ADT_UUID = "adt-uuid";
    private static final String FORM_FIELD_TAB_ID = "tab-id";
    private static final String FORM_FIELD_TAB_ORDER = "tab-order";
    private static final String FORM_FIELD_PAGE_OFFSET = "page-offset";
    private static final String FORM_FIELD_PAGE_SIZE = "page-size";
    private static final String FORM_FIELD_FULL_PAGE_SIZE = "full-page-size";
    private static final String FORM_FIELD_LOAD_MORE_PAGE_SIZE = "load-more-page-size";
    private static final String FORM_FIELD_SEARCH_FACETS = "search-facets";
    private static final String FORM_FIELD_FACET_CLASS_NAME = "facet-class-name";
    private static final String FORM_FIELD_REDIRECT_URL = "redirect-url";
    private static final String FORM_FIELD_ASSET_TYPES = "asset-types";
    private static final Pattern FORM_FIELD_DDM_STRUCTURE_UUID_PATTERN = Pattern.compile("^ddm-" + PatternConstants.UUID + "$");
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

    @Reference(unbind = "-")
    private JSONFactory jsonFactory;

    @Reference(unbind = "-")
    private Language language;

    @Reference(unbind = "-")
    private FlashlightSearchService searchService;

    @Reference(unbind = "-")
    private Portal portal;

    @Reference(target = "(language.type=" + TemplateConstants.LANG_TYPE_FTL + ")", unbind = "-")
    private TemplateManager templateManager;

    @Reference(unbind = "-")
    private DDMTemplateLocalService ddmTemplateLocalService;

    @Reference(unbind = "-")
    private PortletDisplayTemplate portletDisplayTemplate;

    /**
     * Displays the search view (both search fields and search results)
     *
     * @param request  The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException      If something goes wrong
     */
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        HttpServletRequest httpServletRequest = this.portal.getHttpServletRequest(request);
        SearchContext searchContext = SearchContextFactory.getInstance(httpServletRequest);
        String keywords = searchContext.getKeywords();
        String tabId = ParamUtil.get(request, FORM_FIELD_TAB_ID, StringPool.BLANK);

        if (!PATTERN_UUID.matcher(tabId).matches()) {
            tabId = null;
        }

        SearchResultsContainer results;
        if (!keywords.isEmpty()) {
            try {
                if (tabId == null) {
                    results = this.searchService.search(request, response);
                } else {
                    results = this.searchService.search(request, response, tabId);
                }
            } catch (SearchException e) {
                throw new PortletException(e);
            }
        } else {
            results = new SearchResultsContainer(Collections.emptyMap());
        }

        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        HashMap<String, PortletURL> tabUrls = new HashMap<>(tabs.size());
        HashMap<String, ResourceURL> loadMoreUrls = new HashMap<>(tabs.size());

        for(Entry<String, FlashlightSearchConfigurationTab> tabEntry : tabs.entrySet()) {
            String tabUuid = tabEntry.getKey();
            FlashlightSearchConfigurationTab tab = tabEntry.getValue();


            // Tab URLs
            PortletURL tabUrl = response.createRenderURL();
            tabUrl.setParameter(FORM_FIELD_TAB_ID, tabUuid);
            if(!keywords.isEmpty()) {
                tabUrl.setParameter(FORM_FIELD_KEYWORDS, keywords);
            }
            tabUrls.put(tabUuid, tabUrl);

            // Put a load more URL only if we performed a search, that we have search results, and that the total search
            // results is more than a single page can contain
            if(!keywords.isEmpty() && results.hasSearchResults(tabUuid) && results.getSearchPage(tabUuid).getTotalSearchResults() > tab.getFullPageSize()) {
                ResourceURL loadMoreUrl = response.createResourceURL();
                loadMoreUrl.setResourceID(PortletResource.LOAD_MORE.getResourceId());
                loadMoreUrl.setParameter(FORM_FIELD_TAB_ID, tabId);
                loadMoreUrl.setParameter(FORM_FIELD_KEYWORDS, keywords);
                loadMoreUrl.setParameter(FORM_FIELD_PAGE_OFFSET, ONE);
                loadMoreUrls.put(tabId, loadMoreUrl);
            }
        }

        Map<String, Object> templateCtx = this.createTemplateContext();
        templateCtx.put(ViewContextVariable.NAMESPACE.getVariableName(), response.getNamespace());

        PortletURL keywordUrl = response.createRenderURL();
        keywordUrl.setParameter(FORM_FIELD_KEYWORDS, keywords);

        templateCtx.put(ViewContextVariable.KEYWORD_URL.getVariableName(), keywordUrl);
        templateCtx.put(ViewContextVariable.TAB_URLS.getVariableName(), tabUrls);
        templateCtx.put(ViewContextVariable.LOAD_MORE_URLS.getVariableName(), loadMoreUrls);
        templateCtx.put(ViewContextVariable.RESULTS_CONTAINER.getVariableName(), results);
        templateCtx.put(ViewContextVariable.KEYWORDS.getVariableName(), keywords);
        templateCtx.put(ViewContextVariable.TAB_ID.getVariableName(), tabId);
        templateCtx.put(ViewContextVariable.FORMAT_FACET_TERM.getVariableName(), (BiFunction<SearchFacet, String, String>) (searchFacet, term) -> searchService.displayTerm(httpServletRequest, searchFacet, term));

        String adtUuid = config.getAdtUUID();
        if (adtUuid != null && PATTERN_UUID.matcher(adtUuid).matches()) {
            this.renderADT(request, response, templateCtx, adtUuid);
        } else {
            this.renderTemplate(request, response, templateCtx, "view.ftl");
        }
    }

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

        String tabId = ParamUtil.get(request, FORM_FIELD_TAB_ID, StringPool.BLANK);
        int offset = ParamUtil.getInteger(request, FORM_FIELD_PAGE_OFFSET, 0);
        if(offset < 0) {
            offset = 0;
        }

        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();

        if(tabs.containsKey(tabId)) {
            try {
                SearchResultsContainer container = this.searchService.search(request, response, tabId, offset);
                JSONPayloadFactory jsonPayloadFactory = new JSONPayloadFactory(this.jsonFactory);
                if(container.hasSearchResults(tabId)) {
                    FlashlightSearchConfigurationTab tab = tabs.get(tabId);
                    SearchPage page = container.getSearchPage(tabId);

                    ResourceURL loadMoreUrl = response.createResourceURL();
                    loadMoreUrl.setParameter(FORM_FIELD_TAB_ID, tabId);
                    loadMoreUrl.setParameter(FORM_FIELD_KEYWORDS, ParamUtil.get(request, FORM_FIELD_KEYWORDS, StringPool.BLANK));
                    loadMoreUrl.setParameter(FORM_FIELD_PAGE_OFFSET, Integer.toString(offset + 1));

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
        String editModeParam = ParamUtil.get(request, FORM_FIELD_EDIT_MODE, StringPool.BLANK);
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

        PortletURL editGlobalUrl = response.createRenderURL();
        editGlobalUrl.setPortletMode(PortletMode.EDIT);
        ;

        PortletURL createTabUrl = response.createRenderURL();
        createTabUrl.setPortletMode(PortletMode.EDIT);
        createTabUrl.setParameter(FORM_FIELD_EDIT_MODE, EditMode.TAB.getParamValue());

        PortletURL saveGlobalUrl = response.createActionURL();
        saveGlobalUrl.setPortletMode(PortletMode.EDIT);
        saveGlobalUrl.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_SAVE_GLOBAL);

        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        HashMap<String, PortletURL> editTabUrls = new HashMap<>(tabs.size());
        HashMap<String, PortletURL> deleteTabUrls = new HashMap<>(tabs.size());
        tabs.keySet().forEach(tabId -> {
            PortletURL editUrl = response.createRenderURL();
            editUrl.setParameter(FORM_FIELD_EDIT_MODE, EditMode.TAB.getParamValue());
            editUrl.setParameter(FORM_FIELD_TAB_ID, tabId);
            editTabUrls.put(tabId, editUrl);

            PortletURL deleteUrl = response.createActionURL();
            deleteUrl.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_DELETE_TAB);
            deleteUrl.setParameter(FORM_FIELD_TAB_ID, tabId);
            deleteUrl.setParameter(FORM_FIELD_REDIRECT_URL, editGlobalUrl.toString());
            deleteTabUrls.put(tabId, deleteUrl);
        });

        Map<String, List<DDMTemplate>> applicationDisplayTemplates;
        try {
            Map<Group, List<DDMTemplate>> applicationDisplayTemplatesByGroup = this.searchService.getApplicationDisplayTemplates(permissionChecker, groupId);
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

        Map<String, Object> templateCtx = this.createTemplateContext();
        templateCtx.put("ns", response.getNamespace());
        templateCtx.put("editGlobalUrl", editGlobalUrl);
        templateCtx.put("createTabUrl", createTabUrl);
        templateCtx.put("saveGlobalUrl", saveGlobalUrl);
        templateCtx.put("adtUuid", adtUuid);
        templateCtx.put("applicationDisplayTemplates", applicationDisplayTemplates);
        templateCtx.put("availableStructures", availableStructures);
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
        long scopeGroupId = themeDisplay.getScopeGroupId();

        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        String tabId = ParamUtil.get(request, FORM_FIELD_TAB_ID, StringPool.BLANK);
        FlashlightSearchConfigurationTab tab;
        if (tabs.containsKey(tabId)) {
            tab = tabs.get(tabId);
        } else {
            tab = null;
            tabId = StringPool.BLANK;
        }

        int tabOrderRange = tabs.size() + 1;

        Set<Locale> availableLocales = this.language.getAvailableLocales(scopeGroupId);
        Map<String, List<DDMStructure>> availableStructures;
        try {
            availableStructures = this.searchService.getDDMStructures(scopeGroupId);
        } catch (PortalException e) {
            throw new PortletException("Unable to retrieve DDM structures", e);
        }

        PortletURL editGlobalURL = response.createRenderURL();
        editGlobalURL.setPortletMode(PortletMode.EDIT);

        PortletURL saveTabURL = response.createActionURL();
        saveTabURL.setPortletMode(PortletMode.EDIT);
        saveTabURL.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_SAVE_TAB);

        int tabOrder;
        int tabPageSize;
        int tabFullPageSize;
        int tabLoadMorePageSize;
        List<String> assetTypes;
        Map<String, String> searchFacets;
        Map<String, PortletURL> searchFacetsUrls;
        Map<String, String> contentTemplates;
        Map<String, String> titleMap;
        PortletURL redirectUrl = response.createRenderURL();

        if (tab != null) {
            tabOrder = tab.getOrder();
            tabPageSize = tab.getPageSize();
            tabFullPageSize = tab.getFullPageSize();
            tabLoadMorePageSize = tab.getLoadMorePageSize();
            assetTypes = tab.getAssetTypes();
            searchFacets = tab.getSearchFacets();

            searchFacetsUrls = new HashMap<>(searchFacets.size());
            for(String facetClassName : searchFacets.keySet()) {
                PortletURL facetUrl = response.createRenderURL();
                facetUrl.setParameter(FORM_FIELD_EDIT_MODE, EditMode.FACET.getParamValue());
                facetUrl.setParameter(FORM_FIELD_TAB_ID, tabId);
                facetUrl.setParameter(FORM_FIELD_FACET_CLASS_NAME, facetClassName);
                searchFacetsUrls.put(facetClassName, facetUrl);
            }

            contentTemplates = tab.getContentTemplates();
            titleMap = tab.getTitleMap();
            redirectUrl.setParameter(FORM_FIELD_EDIT_MODE, EditMode.TAB.getParamValue());
            redirectUrl.setParameter(FORM_FIELD_TAB_ID, tabId);
        } else {
            tabOrder = tabOrderRange;
            tabPageSize = FlashlightSearchConfigurationTab.DEFAULT_PAGE_SIZE;
            tabFullPageSize = FlashlightSearchConfigurationTab.DEFAULT_FULL_PAGE_SIZE;
            tabLoadMorePageSize = FlashlightSearchConfigurationTab.DEFAULT_LOAD_MORE_PAGE_SIZE;
            assetTypes = Collections.emptyList();
            searchFacets = Collections.emptyMap();
            searchFacetsUrls = Collections.emptyMap();
            contentTemplates = Collections.emptyMap();
            titleMap = Collections.emptyMap();
        }

        List<String> supportedAssetTypes = this.searchService.getSupportedAssetTypes();
        List<SearchFacet> supportedSearchFacets = this.searchService.getSupportedSearchFacets();

        Map<String, Object> templateCtx = this.createTemplateContext();
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
        templateCtx.put("availableStructures", availableStructures);
        templateCtx.put("supportedAssetTypes", supportedAssetTypes);
        templateCtx.put("supportedSearchFacets", supportedSearchFacets);
        templateCtx.put("assetTypes", assetTypes);
        templateCtx.put("searchFacets", searchFacets);
        templateCtx.put("searchFacetUrls", searchFacetsUrls);
        templateCtx.put("contentTemplates", contentTemplates);
        templateCtx.put("titleMap", titleMap);
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
        String tabId = ParamUtil.getString(request, FORM_FIELD_TAB_ID, StringPool.BLANK);
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
            Map<String, Object> templateCtx = this.createTemplateContext();

            PortletURL editTabUrl = response.createRenderURL();
            editTabUrl.setParameter(FORM_FIELD_EDIT_MODE, EditMode.TAB.getParamValue());
            editTabUrl.setParameter(FORM_FIELD_TAB_ID, tabId);

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
        if (!PATTERN_UUID.matcher(adtUuid).matches()) {
            adtUuid = StringPool.BLANK;
        }
        this.searchService.saveADT(adtUuid, request.getPreferences());

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
        String tabId = ParamUtil.get(request, FORM_FIELD_TAB_ID, StringPool.BLANK);
        String tabOrder = ParamUtil.get(request, FORM_FIELD_TAB_ORDER, Integer.toString(FlashlightSearchConfigurationTab.DEFAULT_ORDER));
        String pageSize = ParamUtil.get(request, FORM_FIELD_PAGE_SIZE, Integer.toString(FlashlightSearchConfigurationTab.DEFAULT_PAGE_SIZE));
        String fullPageSize = ParamUtil.get(request, FORM_FIELD_FULL_PAGE_SIZE, Integer.toString(FlashlightSearchConfigurationTab.DEFAULT_FULL_PAGE_SIZE));
        String loadMorePageSize = ParamUtil.get(request, FORM_FIELD_LOAD_MORE_PAGE_SIZE, Integer.toString(FlashlightSearchConfigurationTab.DEFAULT_LOAD_MORE_PAGE_SIZE));
        String[] selectedFacets = ParamUtil.getParameterValues(request, FORM_FIELD_SEARCH_FACETS, StringPool.EMPTY_ARRAY);
        String[] selectAssetTypes = ParamUtil.getParameterValues(request, FORM_FIELD_ASSET_TYPES, StringPool.EMPTY_ARRAY);
        HashMap<String, String> validatedContentTemplates = new HashMap<>();
        HashMap<String, String> validatedTitleMap = new HashMap<>();

        // DDM Template UUIDs and locales are validated on the fly
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = ParamUtil.get(request, paramName, StringPool.BLANK);
            Matcher ddmMatcher = FORM_FIELD_DDM_STRUCTURE_UUID_PATTERN.matcher(paramName);
            Matcher titleMatcher = FORM_FIELD_TITLE_PATTERN.matcher(paramName);

            if (ddmMatcher.matches()) {
                if (PATTERN_UUID.matcher(paramValue).matches()) {
                    validatedContentTemplates.put(ddmMatcher.group(1), paramValue);
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

        ArrayList<String> validatedAssetTypes = new ArrayList<>(selectAssetTypes.length);
        for (String assetType : selectAssetTypes) {
            if (PATTERN_CLASS_NAME.matcher(assetType).matches()) {
                validatedAssetTypes.add(assetType);
            }
        }

        // Create or save the configuration tab and store it
        FlashlightSearchConfigurationTab tab;
        if (validatedTabId != null) {
            tab = new FlashlightSearchConfigurationTab(validatedTabId, validatedTabOrder, validatedPageSize, validatedFullPageSize, validatedLoadMorePageSize, validatedTitleMap, validatedAssetTypes, validatedSelectedFacets, validatedContentTemplates);
        } else {
            tab = new FlashlightSearchConfigurationTab(validatedTabOrder, validatedPageSize, validatedFullPageSize, validatedLoadMorePageSize, validatedTitleMap, validatedAssetTypes, validatedSelectedFacets, validatedContentTemplates);
        }
        this.searchService.saveConfigurationTab(tab, request.getPreferences());

        SessionMessages.add(request, SESSION_MESSAGE_CONFIG_SAVED);

        if (!redirectUrl.isEmpty()) {
            response.sendRedirect(redirectUrl);
        }
    }

    @ProcessAction(name = ACTION_NAME_SAVE_FACET_CONFIG)
    public void actionSaveFacetConfig(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String tabId = ParamUtil.get(request, FORM_FIELD_TAB_ID, StringPool.BLANK);
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
        String tabId = ParamUtil.get(request, FORM_FIELD_TAB_ID, StringPool.BLANK);
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
}
