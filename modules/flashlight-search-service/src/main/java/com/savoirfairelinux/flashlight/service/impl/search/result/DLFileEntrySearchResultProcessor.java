package com.savoirfairelinux.flashlight.service.impl.search.result;

import java.util.*;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.model.DLFileEntryTypeConstants;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.document.library.kernel.util.DL;
import com.liferay.document.library.kernel.util.DLUtil;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portlet.display.template.PortletDisplayTemplate;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.impl.DocumentField;
import com.savoirfairelinux.flashlight.service.impl.facet.DLFileEntryTypeFacet;
import com.savoirfairelinux.flashlight.service.impl.search.result.template.DLFileEntryTemplateVariable;
import com.savoirfairelinux.flashlight.service.model.SearchResult;
import com.savoirfairelinux.flashlight.service.search.result.SearchResultProcessor;
import com.savoirfairelinux.flashlight.service.search.result.exception.SearchResultProcessorException;

/**
 * This processor is used to display file entries in the search results
 */
@Component(
    service = SearchResultProcessor.class,
    immediate = true,
    property = {
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=0"
    }
)
public class DLFileEntrySearchResultProcessor implements SearchResultProcessor {

    private static final Log LOG = LogFactoryUtil.getLog(DLFileEntrySearchResultProcessor.class);

    @Reference
    private DLFileEntryTypeLocalService dlFileEntryTypeService;

    @Reference
    private DLAppService dlAppService;

    @Reference
    private DDMTemplateLocalService ddmTemplateService;

    @Reference
    private Portal portal;

    @Reference
    private PortletDisplayTemplate portletDisplayTemplate;

    private static final String ASSET_TYPE = DLFileEntry.class.getName();

    @Override
    public Collection<Facet> getFacets(SearchContext searchContext, FlashlightSearchConfiguration configuration, FlashlightSearchConfigurationTab tab) {
        DLFileEntryTypeFacet facet = new DLFileEntryTypeFacet(searchContext);
        Map<String, String> fileEntryTypes = tab.getDLFileEntryTypeTemplates();
        long companyId = searchContext.getCompanyId();
        ArrayList<Long> fileEntryTypeIds = new ArrayList<>(fileEntryTypes.size());

        // Again, special case for the default file entry type, which has pretty much all fields set to zero except the
        // UUID.
        try {
            DLFileEntryType basicDocument = this.dlFileEntryTypeService.getFileEntryType(DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT);
            if(fileEntryTypes.containsKey(basicDocument.getUuid())) {
                fileEntryTypeIds.add(DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT);
            }
        } catch(PortalException e) {
            LOG.warn("Cannot obtain basic document UUID", e);
        }

        for(String fileEntryTypeUuid : fileEntryTypes.keySet()) {
            List<DLFileEntryType> companyFileEntryTypes = this.dlFileEntryTypeService.getDLFileEntryTypesByUuidAndCompanyId(fileEntryTypeUuid, companyId);
            if(companyFileEntryTypes.size() == 1) {
                fileEntryTypeIds.add(companyFileEntryTypes.get(0).getFileEntryTypeId());
            }
        }

        int idsLength = fileEntryTypeIds.size();
        long[] ids = new long[idsLength];
        for(int i = 0; i < idsLength; i++) {
            ids[i] = fileEntryTypeIds.get(i);
        }

        facet.setValues(ids);
        return Collections.singletonList(facet);
    }

    @Override
    public SearchResult process(Document searchResultDocument, PortletRequest request, PortletResponse response, SearchContext searchContext, FlashlightSearchConfigurationTab configurationTab) throws SearchResultProcessorException {
        long fileEntryTypeId = Long.parseLong(searchResultDocument.get(DocumentField.FILE_ENTRY_TYPE_ID.getName()));
        SearchResult result;

        try {
            DLFileEntryType fileEntryType = this.dlFileEntryTypeService.getFileEntryType(fileEntryTypeId);
            String fileEntryTypeUuid = fileEntryType.getUuid();
            Map<String, String> configuredTemplates = configurationTab.getDLFileEntryTypeTemplates();

            DDMTemplate renderingTemplate = null;
            if(configuredTemplates.containsKey(fileEntryTypeUuid)) {
                List<DDMTemplate> ddmTemplates = this.ddmTemplateService.getDDMTemplatesByUuidAndCompanyId(configuredTemplates.get(fileEntryTypeUuid), searchContext.getCompanyId());
                if(ddmTemplates.size() == 1) {
                    renderingTemplate = ddmTemplates.get(0);
                }
            }

            if(renderingTemplate != null) {
                HttpServletRequest rq = this.portal.getHttpServletRequest(request);
                HttpServletResponse rp = this.portal.getHttpServletResponse(response);
                try {
                    // Get basic file information
                    ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
                    FileEntry fileEntry = this.getFileEntry(searchResultDocument);
                    FileVersion fileVersion = fileEntry.getFileVersion();
                    DL dl = DLUtil.getDL();

                    // Get URL and title
                    String url = dl.getDownloadURL(fileEntry, fileVersion, themeDisplay, StringPool.BLANK, true, false);
                    String title = searchResultDocument.get(searchContext.getLocale(), Field.TITLE);

                    // Create the ADT context
                    HashMap<String, Object> ctx = new HashMap<String, Object>(4);
                    ctx.put(DLFileEntryTemplateVariable.FILE_ENTRY.getVariableName(), fileEntry);
                    ctx.put(DLFileEntryTemplateVariable.FILE_VERSION.getVariableName(), fileVersion);
                    ctx.put(DLFileEntryTemplateVariable.FILE_URL.getVariableName(), url);
                    ctx.put(DLFileEntryTemplateVariable.FILE_PREVIEW_URL.getVariableName(), dl.getPreviewURL(fileEntry, fileVersion, themeDisplay, StringPool.BLANK, true, false));
                    ctx.put(DLFileEntryTemplateVariable.FILE_IMAGE_PREVIEW_URL.getVariableName(), dl.getImagePreviewURL(fileEntry, fileVersion, themeDisplay));
                    String rendering = this.portletDisplayTemplate.renderDDMTemplate(rq, rp, renderingTemplate, Collections.emptyList(), ctx);
                    result = new SearchResult(rendering, url, title);
                } catch (Exception e) {
                    throw new SearchResultProcessorException(e, searchResultDocument, "Error during document rendering");
                }
            } else {
                throw new SearchResultProcessorException(searchResultDocument, "Cannot obtain document rendering template. No template found or multiple templates with the same UUID found.");
            }
        } catch(PortalException e) {
            throw new SearchResultProcessorException(e, searchResultDocument, "Cannot obtain document's file entry type");
        }

        return result;
    }

    @Override
    public String getAssetType() {
        return ASSET_TYPE;
    }

    /**
     * Returns the file entry related to the indexed document
     *
     * @param searchResultDocument The indexed document
     * @return The file entry related to the indexed document
     *
     * @throws SearchResultProcessorException If the file entry is not found
     */
    private FileEntry getFileEntry(Document searchResultDocument) throws SearchResultProcessorException {
        long groupId = Long.parseLong(searchResultDocument.get(Field.GROUP_ID));
        long fileEntryId = Long.parseLong(searchResultDocument.get(Field.ENTRY_CLASS_PK));
        FileEntry fileEntry;

        try {
            fileEntry = this.dlAppService.getFileEntry(fileEntryId);
        } catch(PortalException e) {
            throw new SearchResultProcessorException(e, searchResultDocument, "Cannot find DLFileEntry");
        }

        if(fileEntry.getGroupId() != groupId) {
            throw new SearchResultProcessorException(searchResultDocument, "Returned DLFileEntry has a groupId different from the one referenced in the index");
        }

        return fileEntry;
    }

}
