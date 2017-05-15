package com.savoirfairelinux.flashlight.service.impl.search.result;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.Constants;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
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
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=0",
        SearchResultProcessor.PROPERTY_ASSET_TYPE + ":String=com.liferay.journal.model.JournalArticle"
    }
)
public class JournalArticleSearchResultProcessor implements SearchResultProcessor {

    @Reference(unbind = "-")
    private JournalArticleLocalService journalArticleService;

    @Override
    public SearchResult process(SearchContext searchContext, FlashlightSearchConfigurationTab configurationTab, DDMStructure structure, Document document) throws SearchResultProcessorException {
        long groupId = Long.parseLong(document.get(Field.GROUP_ID));
        String articleId = document.get(Field.ARTICLE_ID).toString();
        Map<String, String> contentTemplates = configurationTab.getContentTemplates();
        String structureUuid = structure.getUuid();
        SearchResult result;

        if(contentTemplates.containsKey(structureUuid)) {
            String templateUuid = contentTemplates.get(structureUuid);
            DDMTemplate template = structure.getTemplates().stream().filter(t -> t.getUuid().equals(templateUuid)).findFirst().orElse(null);

            if(template != null) {
                try {
                    JournalArticle article = this.journalArticleService.getArticle(groupId, articleId);
                    String articleContents = this.journalArticleService.getArticleContent(article, template.getTemplateKey(), Constants.VIEW, searchContext.getLanguageId(), null, null);
                    result = new SearchResult(articleContents, null, article.getTitle(searchContext.getLanguageId()));
                } catch(PortalException e) {
                    throw new SearchResultProcessorException(e, document);
                }
            } else {
                throw new SearchResultProcessorException(document, "Cannot find template with UUID " + templateUuid + " for the document's structure");
            }
        } else {
            throw new SearchResultProcessorException(document, "No configured template to render given document");
        }

        return result;
    }

}
