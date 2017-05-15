package com.savoirfairelinux.flashlight.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletURL;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.portal.kernel.exception.PortalException;
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
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.savoirfairelinux.flashlight.portlet.framework.TemplatedPortlet;
import com.savoirfairelinux.flashlight.service.FlashlightSearchPortletKeys;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
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
        "javax.portlet.portlet-mode=text/html;view,edit",
        "javax.portlet.security-role-ref=power-user,user",

        "com.liferay.portlet.requires-namespaced-parameters=true",
        "com.liferay.portlet.display-category=category.tools",
        "com.liferay.portlet.instanceable=false",
    }
)
public class FlashlightSearchPortlet extends TemplatedPortlet {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactoryUtil.getLog(FlashlightSearchPortlet.class);

    private static final String ACTION_NAME_SAVE_TAB = "saveTab";
    private static final String ACTION_NAME_SAVE_GLOBAL = "saveGlobal";
    private static final String ACTION_NAME_DELETE_TAB = "deleteTab";

    private static final String FORM_FIELD_KEYWORDS = "keywords";
    private static final String FORM_FIELD_EDIT_MODE = "edit-mode";
    private static final String FORM_FIELD_ADT_UUID = "adt-uuid";
    private static final String FORM_FIELD_TAB_ID = "tab-id";
    private static final String FORM_FIELD_TAB_ORDER = "tab-order";
    private static final String FORM_FIELD_PAGE_SIZE = "page-size";
    private static final String FORM_FIELD_FACETS = "facets";
    private static final String FORM_FIELD_REDIRECT_URL = "redirect-url";
    private static final String FORM_FIELD_ASSET_TYPES = "asset-types";
    private static final Pattern FORM_FIELD_DDM_STRUCTURE_UUID_PATTERN = Pattern.compile("^ddm-" + PatternConstants.UUID + "$");
    private static final Pattern FORM_FIELD_TITLE_PATTERN = Pattern.compile("^title-" + PatternConstants.LOCALE + "$");

    private static final Pattern PATTERN_CLASS_NAME = Pattern.compile("^" + PatternConstants.CLASS_NAME + "$");
    private static final Pattern PATTERN_UUID = Pattern.compile("^" + PatternConstants.UUID + "$");

    private static final String EDIT_MODE_TAB = "tab";

    private static final String SESSION_MESSAGE_CONFIG_SAVED = "configuration.saved";

    private static final String ZERO = "0";
    private static final String THREE = "3";
    private static final String[] EMPTY_ARRAY = new String[0];

    @Reference(unbind = "-")
    private Language language;

    @Reference(unbind = "-")
    private AssetCategoryLocalService assetCategoryLocalService;

    @Reference(unbind = "-")
    private FlashlightSearchService searchService;

    @Reference(unbind = "-")
    private Portal portal;

    @Reference(target = "(language.type=" + TemplateConstants.LANG_TYPE_FTL + ")", unbind = "-")
    private TemplateManager templateManager;

    /**
     * Displays the search view (both search fields and search results)
     *
     * @param request The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException If something goes wrong
     */
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        SearchContext searchContext = SearchContextFactory.getInstance(this.portal.getHttpServletRequest(request));
        String keywords = searchContext.getKeywords();

        SearchResultsContainer results;
        if (!keywords.isEmpty()) {
            try {
                results = this.searchService.search(request, response);
            } catch (SearchException e) {
                throw new PortletException(e);
            }
        } else {
            results = new SearchResultsContainer(Collections.emptyMap());
        }

        Map<String, Object> templateCtx = this.createTemplateContext();
        // General objects
        templateCtx.put("ns", response.getNamespace());

        // Search objects
        PortletURL keywordUrl = response.createRenderURL();
        keywordUrl.setParameter(FORM_FIELD_KEYWORDS, keywords);

        templateCtx.put("keywordUrl", keywordUrl);
        templateCtx.put("resultsContainer", results);
        templateCtx.put("keywords", keywords);
        templateCtx.put("categories", this.assetCategoryLocalService.getCategories());
        templateCtx.put("flashlightUtil",  new FlashlightUtil());
        this.renderTemplate(request, response, templateCtx, "view.ftl");
    }

    /**
     * Routes between global configuration editing and tab editing
     *
     * @param request The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException If something goes wrong
     */
    @Override
    public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Very, very simple routing. That's all we need, folks.
        String editMode = ParamUtil.get(request, FORM_FIELD_EDIT_MODE, StringPool.BLANK);
        switch(editMode) {
            case EDIT_MODE_TAB:
                this.doEditTab(request, response);
            break;
            default:
                this.doEditGlobal(request, response);
            break;
        }
    }

    /**
     * Render phase, edit mode, global configuration
     *
     * @param request The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException If something goes wrong
     */
    public void doEditGlobal(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();
        long groupId = themeDisplay.getScopeGroupId();

        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        String adtUuid = config.getAdtUUID();

        PortletURL editGlobalUrl = response.createRenderURL();
        editGlobalUrl.setPortletMode(PortletMode.EDIT);;

        PortletURL createTabUrl = response.createRenderURL();
        createTabUrl.setPortletMode(PortletMode.EDIT);
        createTabUrl.setParameter(FORM_FIELD_EDIT_MODE, EDIT_MODE_TAB);

        PortletURL saveGlobalUrl = response.createActionURL();
        saveGlobalUrl.setPortletMode(PortletMode.EDIT);
        saveGlobalUrl.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_SAVE_GLOBAL);

        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        HashMap<String, PortletURL> editTabUrls = new HashMap<>(tabs.size());
        HashMap<String, PortletURL> deleteTabUrls = new HashMap<>(tabs.size());
        tabs.keySet().forEach(tabId -> {
            PortletURL editUrl = response.createRenderURL();
            editUrl.setParameter(FORM_FIELD_EDIT_MODE, EDIT_MODE_TAB);
            editUrl.setParameter(FORM_FIELD_TAB_ID, tabId);
            editTabUrls.put(tabId, editUrl);

            PortletURL deleteUrl = response.createActionURL();
            deleteUrl.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_DELETE_TAB);
            deleteUrl.setParameter(FORM_FIELD_TAB_ID, tabId);
            deleteUrl.setParameter(FORM_FIELD_REDIRECT_URL, editGlobalUrl.toString());
            deleteTabUrls.put(tabId, deleteUrl);
        });

        Map<Group, List<DDMTemplate>> applicationDisplayTemplates;
        try {
            applicationDisplayTemplates = this.searchService.getApplicationDisplayTemplates(permissionChecker, groupId);
        } catch(PortalException e) {
            throw new PortletException("Cannot fetch application display templates", e);
        }

        Map<String, Object> templateCtx = this.createTemplateContext();
        templateCtx.put("ns", response.getNamespace());
        templateCtx.put("editGlobalUrl", editGlobalUrl);
        templateCtx.put("createTabUrl", createTabUrl);
        templateCtx.put("saveGlobalUrl", saveGlobalUrl);
        templateCtx.put("adtUuid", adtUuid);
        templateCtx.put("applicationDisplayTemplates", applicationDisplayTemplates);
        templateCtx.put("tabs", tabs);
        templateCtx.put("editTabUrls", editTabUrls);
        templateCtx.put("deleteTabUrls", deleteTabUrls);
        this.renderTemplate(request, response, templateCtx, "edit.ftl");
    }

    /**
     * Render phase, edit mode, tab configuration
     *
     * @param request The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException If something goes wrong
     */
    public void doEditTab(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        long scopeGroupId = themeDisplay.getScopeGroupId();

        FlashlightSearchConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        Map<String, FlashlightSearchConfigurationTab> tabs = config.getTabs();
        String tabId = ParamUtil.get(request, FORM_FIELD_TAB_ID, StringPool.BLANK);
        FlashlightSearchConfigurationTab tab;
        if(tabs.containsKey(tabId)) {
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
        List<String> assetTypes;
        Map<String, String> contentTemplates;
        Map<String, String> titleMap;
        PortletURL redirectUrl = response.createRenderURL();

        if(tab != null) {
            tabOrder = tab.getOrder();
            tabPageSize = tab.getPageSize();
            assetTypes = tab.getAssetTypes();
            contentTemplates = tab.getContentTemplates();
            titleMap = tab.getTitleMap();
            redirectUrl.setParameter(FORM_FIELD_EDIT_MODE, EDIT_MODE_TAB);
            redirectUrl.setParameter(FORM_FIELD_TAB_ID, tabId);
        } else {
            tabOrder = tabOrderRange;
            tabPageSize = FlashlightSearchConfigurationTab.DEFAULT_PAGE_SIZE;
            assetTypes = Collections.emptyList();
            contentTemplates = Collections.emptyMap();
            titleMap = Collections.emptyMap();
        }

        List<String> supportedAssetTypes = this.searchService.getSupportedAssetTypes();

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
        templateCtx.put("tabOrderRange", tabOrderRange);
        templateCtx.put("availableLocales", availableLocales);
        templateCtx.put("availableStructures", availableStructures);
        templateCtx.put("supportedAssetTypes", supportedAssetTypes);
        templateCtx.put("assetTypes", assetTypes);
        templateCtx.put("contentTemplates", contentTemplates);
        templateCtx.put("titleMap", titleMap);
        this.renderTemplate(request, response, templateCtx, "edit-tab.ftl");
    }

    /**
     * Saves the global aspect of the configuration
     *
     * @param request The request
     * @param response The response
     * @throws IOException If something goes wrong
     * @throws PortletException If something goes wrong
     */
    @ProcessAction(name = ACTION_NAME_SAVE_GLOBAL)
    public void actionSaveGlobal(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        String redirectUrl = ParamUtil.get(request, FORM_FIELD_REDIRECT_URL, StringPool.BLANK);
        String adtUuid = ParamUtil.get(request, FORM_FIELD_ADT_UUID, StringPool.BLANK);
        if(!PATTERN_UUID.matcher(adtUuid).matches()) {
            adtUuid = StringPool.BLANK;
        }
        this.searchService.saveADT(adtUuid, request.getPreferences());

        SessionMessages.add(request, SESSION_MESSAGE_CONFIG_SAVED);
        if(!redirectUrl.isEmpty()) {
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Saves a Flashlight Search configuration tab as portlet preferences.
     *
     * The save action performs very basic validation - it checks for format validity, but it will not check whether
     * objects referenced by the parameters exist. We are not holding the user's hands on this one - the form consists
     * of select fields generated by Liferay data. If the user tampers with such data in a way that breaks the format,
     * such data will not be saved. No error will be displayed in this case.
     *
     * @param request The request
     * @param response The response
     *
     * @throws IOException If configuration fails to save
     * @throws PortletException If a portlet error occurs
     */
    @ProcessAction(name = ACTION_NAME_SAVE_TAB)
    public void actionSaveTab(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        // Get raw parameters
        String redirectUrl = ParamUtil.get(request, FORM_FIELD_REDIRECT_URL, StringPool.BLANK);
        String tabId = ParamUtil.get(request, FORM_FIELD_TAB_ID, StringPool.BLANK);
        String tabOrder = ParamUtil.get(request, FORM_FIELD_TAB_ORDER, ZERO);
        String pageSize = ParamUtil.get(request, FORM_FIELD_PAGE_SIZE, THREE);
        String[] selectedFacets = ParamUtil.getParameterValues(request, FORM_FIELD_FACETS, EMPTY_ARRAY);
        String[] selectAssetTypes = ParamUtil.getParameterValues(request, FORM_FIELD_ASSET_TYPES, EMPTY_ARRAY);
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
                if(PATTERN_UUID.matcher(paramValue).matches()) {
                    validatedContentTemplates.put(ddmMatcher.group(1), paramValue);
                }
            } else if(titleMatcher.matches()) {
                // Escape the title
                validatedTitleMap.put(titleMatcher.group(1), HtmlUtil.escape(paramValue));
            }
        }

        // Validate the fields
        String validatedTabId;
        if(PATTERN_UUID.matcher(tabId).matches()) {
            validatedTabId = tabId;
        } else {
            validatedTabId = null;
        }

        int validatedTabOrder;
        try {
            validatedTabOrder = Integer.parseInt(tabOrder);
        } catch(NumberFormatException e) {
            validatedTabOrder = 0;
        }

        int validatedPageSize;
        try {
            validatedPageSize = Integer.parseInt(pageSize);
        } catch(NumberFormatException e) {
            validatedPageSize = FlashlightSearchConfigurationTab.DEFAULT_PAGE_SIZE;
        }

        ArrayList<String> validatedSelectedFacets = new ArrayList<>(selectedFacets.length);
        for(String facet : selectedFacets) {
            if(PATTERN_CLASS_NAME.matcher(facet).matches()) {
                validatedSelectedFacets.add(facet);
            }
        }

        ArrayList<String> validatedAssetTypes = new ArrayList<>(selectAssetTypes.length);
        for(String assetType : selectAssetTypes) {
            if(PATTERN_CLASS_NAME.matcher(assetType).matches()) {
                validatedAssetTypes.add(assetType);
            }
        }

        // Create or save the configuration tab and store it
        FlashlightSearchConfigurationTab tab;
        if(validatedTabId != null) {
            tab = new FlashlightSearchConfigurationTab(validatedTabId, validatedTabOrder, validatedPageSize, validatedTitleMap, validatedAssetTypes, validatedContentTemplates);
        } else {
            tab = new FlashlightSearchConfigurationTab(validatedTabOrder, validatedPageSize, validatedTitleMap, validatedAssetTypes, validatedContentTemplates);
        }
        this.searchService.saveConfigurationTab(tab, request.getPreferences());

        SessionMessages.add(request, SESSION_MESSAGE_CONFIG_SAVED);

        if(!redirectUrl.isEmpty()) {
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Deletes a tab from the configuration
     *
     * @param request The request
     * @param response The response
     * @throws PortletException If something goes wrong
     * @throws IOException If something goes wrong
     */
    @ProcessAction(name = ACTION_NAME_DELETE_TAB)
    public void actionDeleteTab(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String tabId = ParamUtil.get(request, FORM_FIELD_TAB_ID, StringPool.BLANK);
        String redirectUrl = ParamUtil.get(request, FORM_FIELD_REDIRECT_URL, StringPool.BLANK);

        if(tabId != null && PATTERN_UUID.matcher(tabId).matches()) {
            PortletPreferences preferences = request.getPreferences();
            Map<String, FlashlightSearchConfigurationTab> tabs = this.searchService.readConfiguration(preferences).getTabs();
            if(tabs.containsKey(tabId)) {
                this.searchService.deleteConfigurationTab(tabId, preferences);
            }
        }

        SessionMessages.add(request, SESSION_MESSAGE_CONFIG_SAVED);

        if(!redirectUrl.isEmpty()) {
            response.sendRedirect(redirectUrl);
        }
    }

    @Override
    protected Portal getPortal() {
        return this.portal;
    }

    @Override
    protected TemplateManager getTemplateManager() {
        return this.templateManager;
    }

}
