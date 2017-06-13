package com.savoirfairelinux.flashlight.service.impl.search.result;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalArticleDisplay;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.util.JournalContent;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortletRequestModel;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.impl.DocumentField;
import com.savoirfairelinux.flashlight.service.impl.facet.DDMStructureFacet;
import com.savoirfairelinux.flashlight.service.model.SearchResult;
import com.savoirfairelinux.flashlight.service.search.result.SearchResultProcessor;
import com.savoirfairelinux.flashlight.service.search.result.exception.SearchResultProcessorException;

/**
 * This processor is used to create search results that originates from JournalArticle documents
 */
@Component(
    service = SearchResultProcessor.class,
    immediate = true,
    property = {
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=0"
    }
)
public class JournalArticleSearchResultProcessor implements SearchResultProcessor {

    private static final String ASSET_TYPE = JournalArticle.class.getName();

    private static final Log LOG = LogFactoryUtil.getLog(JournalArticleSearchResultProcessor.class);

    @Reference
    private DDMStructureLocalService ddmStructureService;

    @Reference
    private JournalArticleLocalService journalArticleService;

    @Reference
    private JournalContent journalContent;

    @Reference
    private ClassNameLocalService classNameService;

    @Override
    public Facet getFacet(SearchContext searchContext, FlashlightSearchConfiguration configuration, FlashlightSearchConfigurationTab tab) {
        Map<String, String> contentTemplates = tab.getContentTemplates();
        DDMStructureFacet structureFacet = new DDMStructureFacet(searchContext);
        String[] ddmStructures = contentTemplates.keySet()
            .stream()
            .map(structureUuid -> {
                String structureKey;
                List<DDMStructure> structures = this.ddmStructureService.getDDMStructuresByUuidAndCompanyId(structureUuid, searchContext.getCompanyId());
                if(structures.size() == 1) {
                    structureKey = structures.get(0).getStructureKey();
                } else {
                    // Ambiguous or unavailable structure
                    structureKey = null;
                }
                return structureKey;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet())
            .toArray(new String[contentTemplates.size()]);

        structureFacet.setValues(ddmStructures);

        return structureFacet;
    }

    @Override
    public SearchResult process(PortletRequest request, PortletResponse response, SearchContext searchContext, FlashlightSearchConfigurationTab configurationTab, Document document) throws SearchResultProcessorException {
        long groupId = Long.parseLong(document.get(Field.GROUP_ID));
        String articleId = document.get(Field.ARTICLE_ID);
        Map<String, String> contentTemplates = configurationTab.getContentTemplates();
        String structureKey = document.getField(DocumentField.DDM_STRUCTURE_KEY.getName()).getValue();

        DDMStructure structure;
        try {
            long classNameId = this.classNameService.getClassNameId(ASSET_TYPE);
            structure = this.ddmStructureService.getStructure(groupId, classNameId, structureKey, true);
        } catch(PortalException e) {
            throw new SearchResultProcessorException(e, document, "Cannot find structure for given document");
        }

        String structureUuid = structure.getUuid();
        if(!contentTemplates.containsKey(structureUuid)) {
            throw new SearchResultProcessorException(document, "No configured template to render given document");
        }

        String templateUuid = contentTemplates.get(structureUuid);
        DDMTemplate template = structure.getTemplates().stream().filter(t -> t.getUuid().equals(templateUuid)).findFirst().orElse(null);
        if(template == null) {
            throw new SearchResultProcessorException(document, "Cannot find template with UUID " + templateUuid + " for the document's structure");
        }

        SearchResult result;
        try {
            JournalArticle article = this.journalArticleService.getArticle(groupId, articleId);
            double version = article.getVersion();
            String assetViewURL = getAssetViewURL(request, response, document);
            request.setAttribute("flashlightSearchViewURL", assetViewURL);
            PortletRequestModel portletRequestModel = new PortletRequestModel(request, response);
            ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
            JournalArticleDisplay journalContentDisplay = journalContent.getDisplay(groupId, articleId, version, template.getTemplateKey(), Constants.VIEW, searchContext.getLanguageId(), 0, portletRequestModel, themeDisplay);
            String articleContents = journalContentDisplay.getContent();
            result = new SearchResult(articleContents, assetViewURL, article.getTitle(searchContext.getLanguageId()));
        } catch(PortalException e) {
            throw new SearchResultProcessorException(e, document);
        }

        return result;
    }

    @Override
    public String getAssetType() {
        return ASSET_TYPE;
    }


    private String getAssetViewURL(PortletRequest renderRequest, PortletResponse renderResponse, Document document) {

        String className = document.get("entryClassName");
        int classPK = GetterUtil.getInteger(document.get("entryClassPK"));
        try {
            String currentURL = PortalUtil.getCurrentURL(renderRequest);
            ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);

            PortletURL viewContentURL = PortletURLFactoryUtil.create(renderRequest,
                "com_liferay_portal_search_web_portlet_SearchPortlet", themeDisplay.getLayout(),
                PortletRequest.RENDER_PHASE);

            viewContentURL.setParameter("mvcPath", "/view_content.jsp");
            viewContentURL.setParameter("redirect", currentURL);
            viewContentURL.setPortletMode(PortletMode.VIEW);
            viewContentURL.setWindowState(WindowState.MAXIMIZED);

            if (Validator.isNull(className) || (classPK <= 0)) {
                return viewContentURL.toString();
            }

            AssetEntry assetEntry = AssetEntryLocalServiceUtil.getEntry(className, classPK);

            AssetRendererFactory<?> assetRendererFactory = AssetRendererFactoryRegistryUtil
                .getAssetRendererFactoryByClassName(className);

            if (assetRendererFactory == null) {
                return viewContentURL.toString();
            }

            viewContentURL.setParameter("assetEntryId", String.valueOf(assetEntry.getEntryId()));
            viewContentURL.setParameter("type", assetRendererFactory.getType());

            AssetRenderer<?> assetRenderer = assetRendererFactory.getAssetRenderer(classPK);

            String viewURL = assetRenderer.getURLViewInContext((LiferayPortletRequest) renderRequest,
                (LiferayPortletResponse) renderResponse, viewContentURL.toString());

            return checkViewURL(assetEntry, true, viewURL, currentURL, themeDisplay);
        } catch (Exception e) {
            LOG.error("Unable to get search result  view URL for class " + className + " with primary key " + classPK,
                e);

            return "";
        }
    }

    private String checkViewURL(AssetEntry assetEntry, boolean viewInContext, String viewURL, String currentURL,
                                ThemeDisplay themeDisplay) {

        if (Validator.isNull(viewURL)) {
            return viewURL;
        }

        viewURL = HttpUtil.setParameter(viewURL, "inheritRedirect", viewInContext);

        Layout layout = themeDisplay.getLayout();

        String assetEntryLayoutUuid = assetEntry.getLayoutUuid();

        if (!viewInContext
            || (Validator.isNotNull(assetEntryLayoutUuid) && !assetEntryLayoutUuid.equals(layout.getUuid()))) {

            viewURL = HttpUtil.setParameter(viewURL, "redirect", currentURL);
        }

        return viewURL;
    }

}
