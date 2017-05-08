package com.savoirfairelinux.flashlight.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletURL;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.template.TemplateManager;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.savoirfairelinux.flashlight.portlet.configuration.FlashlightConfiguration;
import com.savoirfairelinux.flashlight.portlet.framework.TemplatedPortlet;
import com.savoirfairelinux.flashlight.service.FlashlightSearchService;

@Component(
    service = Portlet.class,
    immediate = true,
    property = {
        "javax.portlet.name=" + FlashlightPortletKeys.PORTLET_NAME,
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

    private static final String ACTION_NAME_SAVE_CONFIGURATION = "saveConfiguration";

    private static final String FORM_FIELD_SELECTED_FACETS = "selected-facets";
    private static final String FORM_FIELD_SELECTED_ASSET_TYPES = "selected-asset-types";
    private static final String FORM_FIELD_ADT_UUID = "adt-uuid";
    private static final Pattern FORM_FIELD_STRUCTURE_UUID_PATTERN = Pattern.compile("^ddm-([a-f0-9]{8}-([a-f0-9]{4}-){3}[a-f0-9]{12})$");

    private static final Pattern PATTERN_UUID = Pattern.compile("^[a-f0-9]{8}-([a-f0-9]{4}-){3}[a-f0-9]{12}$");
    private static final Pattern PATTERN_CLASS_NAME = Pattern.compile("^[a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)*$");

    private static final String[] EMPTY_ARRAY = new String[0];

    private AssetCategoryLocalService assetCategoryLocalService;
    private FlashlightSearchService searchService;

    private Portal portal;
    private TemplateManager templateManager;

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        FlashlightConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        HttpServletRequest servletRequest = this.portal.getHttpServletRequest(request);
        String keywords = ParamUtil.get(request, "keywords", StringPool.BLANK);

        Map<String, List<Document>> groupedDocuments;

        if (!keywords.isEmpty()) {
            SearchContext searchContext = SearchContextFactory.getInstance(servletRequest);
            searchContext.setKeywords(keywords);
            try {
                groupedDocuments = this.searchService.customGroupedSearch(searchContext, request.getPreferences(), Field.ENTRY_CLASS_NAME, 800);
            } catch (SearchException e) {
                throw new PortletException(e);
            }
        } else {
            groupedDocuments = Collections.emptyMap();
        }

        List<String> selectedFacets = config.getSelectedFacets();
        List<String> searchFacets = SearchFacetTracker.getSearchFacets()
            .stream()
            .filter(facet -> selectedFacets.contains(facet.getClassName()))
            .map(facet -> facet.getClassName())
            .collect(Collectors.toList());

        Map<String, Object> templateCtx = this.createTemplateContext();
        // General objects
        templateCtx.put("ns", response.getNamespace());

        // Search objects
        PortletURL keywordUrl = response.createRenderURL();
        keywordUrl.setParameter("keywords", keywords);

        templateCtx.put("keywordUrl", keywordUrl.toString());
        templateCtx.put("groupedDocuments", groupedDocuments);
        templateCtx.put("searchFacets", searchFacets);
        templateCtx.put("keywords", keywords);
        templateCtx.put("categories", this.assetCategoryLocalService.getCategories());
        templateCtx.put("flashlightUtil",  new FlashlightUtil());
        this.renderTemplate(request, response, templateCtx, "view.ftl");
    }

    @Override
    public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        long scopeGroupId = themeDisplay.getScopeGroupId();
        FlashlightConfiguration config = this.searchService.readConfiguration(request.getPreferences());
        List<String> selectedFacets = config.getSelectedFacets();
        Map<String, String> contentTemplates = config.getContentTemplates();

        Map<String, List<DDMStructure>> availableStructures;
        try {
            availableStructures = this.searchService.getDDMStructures(scopeGroupId);
        } catch (PortalException e) {
            throw new PortletException("Unable to retrieve DDM structures", e);
        }


        PortletURL renderURL = response.createRenderURL();
        renderURL.setPortletMode(PortletMode.EDIT);

        PortletURL configureURL = response.createActionURL();
        configureURL.setParameter(ActionRequest.ACTION_NAME, ACTION_NAME_SAVE_CONFIGURATION);

        Map<String, Object> templateCtx = this.createTemplateContext();
        // Base template data
        templateCtx.put("ns", response.getNamespace());
        templateCtx.put("editRenderURL", renderURL.toString());
        templateCtx.put("configureURL", configureURL.toString());

        // Configuration data
        templateCtx.put("supportedAssetTypes", FlashlightSearchService.SUPPORTED_ASSET_TYPES);
        templateCtx.put("selectedFacets", selectedFacets);
        templateCtx.put("selectedAssetTypes", config.getSelectedAssetTypes());
        templateCtx.put("searchFacets", SearchFacetTracker.getSearchFacets());
        templateCtx.put("contentTemplates", contentTemplates);
        templateCtx.put("structures", availableStructures);
        this.renderTemplate(request, response, templateCtx, "edit.ftl");
    }

    /**
     * Saves Flashlight Search's configuration as portlet preferences.
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
    @ProcessAction(name = ACTION_NAME_SAVE_CONFIGURATION)
    public void actionSaveConfiguration(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        // Get raw parameters
        String[] selectedFacets = ParamUtil.getParameterValues(request, FORM_FIELD_SELECTED_FACETS, EMPTY_ARRAY);
        String[] selectAssetTypes = ParamUtil.getParameterValues(request, FORM_FIELD_SELECTED_ASSET_TYPES, EMPTY_ARRAY);
        String adtUuid = ParamUtil.get(request, FORM_FIELD_ADT_UUID, StringPool.BLANK);
        HashMap<String, String> validatedContentTemplates = new HashMap<String, String>();

        // DDM Template UUIDs are validated on the fly
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            Matcher paramMatcher = FORM_FIELD_STRUCTURE_UUID_PATTERN.matcher(paramName);
            if (paramMatcher.matches()) {
                String paramValue = ParamUtil.get(request, paramName, StringPool.BLANK);
                if(PATTERN_UUID.matcher(paramValue).matches()) {
                    validatedContentTemplates.put(paramMatcher.group(1), paramValue);
                }
            }
        }

        // Validate the fields
        ArrayList<String> validatedSelectedFacets = new ArrayList<>(selectedFacets.length);
        for(String facet : selectedFacets) {
            if(PATTERN_CLASS_NAME.matcher(facet).matches()) {
                validatedSelectedFacets.add(facet);
            }
        }

        ArrayList<String> validatedSelectedAssetTypes = new ArrayList<>(selectAssetTypes.length);
        for(String assetType : selectAssetTypes) {
            if(PATTERN_CLASS_NAME.matcher(assetType).matches()) {
                validatedSelectedAssetTypes.add(assetType);
            }
        }

        String validatedAdtUuid;
        if(PATTERN_UUID.matcher(adtUuid).matches()) {
            validatedAdtUuid = adtUuid;
        } else {
            validatedAdtUuid = StringPool.BLANK;
        }

        // Create the configuration and store it
        FlashlightConfiguration config = new FlashlightConfiguration(
            validatedSelectedFacets,
            validatedSelectedAssetTypes,
            validatedContentTemplates,
            validatedAdtUuid
        );
        this.searchService.writeConfiguration(config, request.getPreferences());
    }

    @Reference(unbind = "-")
    public void setAssetCategoryLocalService(AssetCategoryLocalService assetCategoryLocalService){
        this.assetCategoryLocalService  = assetCategoryLocalService;
    }

    @Reference(unbind = "-")
    public void setSearchService(FlashlightSearchService service) {
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
