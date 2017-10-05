package com.savoirfairelinux.flashlight.service.impl.search.result;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
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
import com.liferay.portal.kernel.portlet.PortletURLFactory;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.impl.DocumentField;
import com.savoirfairelinux.flashlight.service.impl.facet.ClassNameIdFacet;
import com.savoirfairelinux.flashlight.service.impl.facet.DDMStructureFacet;
import com.savoirfairelinux.flashlight.service.model.SearchResult;
import com.savoirfairelinux.flashlight.service.portlet.PortletRequestParameter;
import com.savoirfairelinux.flashlight.service.portlet.ViewMode;
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

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactoryUtil.getLog(JournalArticleSearchResultProcessor.class);

    private static final String ASSET_TYPE = JournalArticle.class.getName();

    private static final String LIFECYCLE_RENDER = "0";

    @Reference
    private AssetEntryLocalService assetEntryService;

    @Reference
    private DDMStructureLocalService ddmStructureService;

    @Reference
    private Http http;

    @Reference
    private JournalArticleLocalService journalArticleService;

    @Reference
    private JournalContent journalContent;

    @Reference
    private ClassNameLocalService classNameService;

    @Reference
    private Portal portal;

    @Reference
    private PortletURLFactory portletUrlFactory;

    @Override
    public Collection<Facet> getFacets(SearchContext searchContext, FlashlightSearchConfiguration configuration, FlashlightSearchConfigurationTab tab) {
        Map<String, String> templates = tab.getJournalArticleTemplates();
        DDMStructureFacet structureFacet = new DDMStructureFacet(searchContext);
        String[] ddmStructures = templates.keySet()
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
            .toArray(new String[templates.size()]);

        structureFacet.setValues(ddmStructures);

        ClassNameIdFacet classNameIdFacet = new ClassNameIdFacet(searchContext);
        classNameIdFacet.setValues(new int[]{0});

        return Arrays.asList(new Facet[]{structureFacet, classNameIdFacet});
    }

    @Override
    public SearchResult process(Document document, PortletRequest request, PortletResponse response, SearchContext searchContext, FlashlightSearchConfigurationTab configurationTab) throws SearchResultProcessorException {
        long groupId = Long.parseLong(document.get(Field.GROUP_ID));
        String articleId = document.get(Field.ARTICLE_ID);
        Map<String, String> templates = configurationTab.getJournalArticleTemplates();
        String structureKey = document.getField(DocumentField.DDM_STRUCTURE_KEY.getName()).getValue();

        DDMStructure structure;
        try {
            long classNameId = this.classNameService.getClassNameId(ASSET_TYPE);
            structure = this.ddmStructureService.getStructure(groupId, classNameId, structureKey, true);
        } catch(PortalException e) {
            throw new SearchResultProcessorException(e, document, "Cannot find structure for given document");
        }

        String structureUuid = structure.getUuid();
        if(!templates.containsKey(structureUuid)) {
            throw new SearchResultProcessorException(document, "No configured template to render given document");
        }

        String templateUuid = templates.get(structureUuid);
        DDMTemplate template = structure.getTemplates().stream().filter(t -> t.getUuid().equals(templateUuid)).findFirst().orElse(null);
        if(template == null) {
            throw new SearchResultProcessorException(document, "Cannot find template with UUID " + templateUuid + " for the document's structure");
        }

        SearchResult result;
        try {
            ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
            JournalArticle article = this.journalArticleService.getArticle(groupId, articleId);
            double version = article.getVersion();
            String assetViewURL = this.getAssetViewURL(document, request, response, themeDisplay.getLayout(), configurationTab.getId(), searchContext.getKeywords());
            request.setAttribute("flashlightSearchViewURL", assetViewURL);
            request.setAttribute("flashlightSearchDocument", document);
            PortletRequestModel portletRequestModel = new PortletRequestModel(request, response);
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

    /**
     * Gets the search result's view URL
     *
     * @param document The searched document
     * @param request The request
     * @param response The response
     * @param currentLayout The current page
     * @param tabId The search tab ID
     *
     * @return The search result URL
     */
    private String getAssetViewURL(Document document, PortletRequest request, PortletResponse response, Layout currentLayout, String tabId, String keywords) {
        String className = document.get(Field.ENTRY_CLASS_NAME);
        long classPK = GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK));
        String currentUrl = this.portal.getCurrentURL(request);
        String returnedUrl;

        if(ASSET_TYPE.equals(className) && classPK > 0) {
            try {
                String portletId = (String) request.getAttribute(WebKeys.PORTLET_ID);
                PortletURL viewInPortletUrlObj = this.portletUrlFactory.create(request, portletId, currentLayout, LIFECYCLE_RENDER);
                viewInPortletUrlObj.setParameter(PortletRequestParameter.VIEW_MODE.getName(), ViewMode.VIEW_JOURNAL.getParamValue());
                viewInPortletUrlObj.setParameter(Field.ENTRY_CLASS_PK, Long.toString(classPK));
                viewInPortletUrlObj.setParameter(PortletRequestParameter.TAB_ID.getName(), tabId);
                viewInPortletUrlObj.setParameter(PortletRequestParameter.KEYWORDS.getName(), keywords);
                String viewInPortletUrl = viewInPortletUrlObj.toString();

                AssetRendererFactory<?> assetRendererFactory = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(className);
                AssetRenderer<?> assetRenderer = assetRendererFactory.getAssetRenderer(classPK);
                returnedUrl = assetRenderer.getURLViewInContext((LiferayPortletRequest) request, (LiferayPortletResponse) response, viewInPortletUrl);
            } catch(Exception e) {
                returnedUrl = currentUrl;
            }
        } else {
            returnedUrl = currentUrl;
        }

        return returnedUrl;
    }

}
