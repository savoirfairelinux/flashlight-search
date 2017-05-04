package com.savoirfairelinux.flashlight.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.portlet.PortletPreferences;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.ScopeFacet;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManagerUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.savoirfairelinux.flashlight.service.SearchService;

@Component(
    service = SearchService.class,
    immediate = true
)
public class SearchServiceImpl implements SearchService {

    private static final Log LOG = LogFactoryUtil.getLog(SearchServiceImpl.class);

    private JournalArticleLocalService journalArticleLocalService;

    @Override
    public Map<String, List<Document>> customGroupedSearch(SearchContext searchContext, PortletPreferences portletPreferences, String groupBy, int maxCount) {
        Hits hits = this.customSearch(searchContext, portletPreferences);
        Map<String, List<Document>> results;

        if (hits != null) {

            Document[] documents = hits.getDocs();
            results = new HashMap<>();

            for (Document document : documents) {
                String key = groupBy;
                if (document.get(Field.ENTRY_CLASS_NAME).equals(JournalArticle.class.getName())) {
                    key = "ddmStructureKey";
                    JournalArticle journalArticle = this.journalArticleLocalService.fetchArticle(GetterUtil.getLong(document.get("groupId")), document.get("articleId"));
                    String templateKey = portletPreferences.getValue("ddm-"+document.get("ddmStructureKey"), document.get("ddmTemplateKey"));
                    try {
                        String content  = this.journalArticleLocalService.getArticleContent(journalArticle, templateKey, Constants.VIEW, searchContext.getLanguageId(), null, null);
                        document.addKeyword("journalContent", content);
                    } catch (PortalException e) {
                        LOG.error("Cannot retrieve article content",e);
                    }
                } else if (document.get(Field.ENTRY_CLASS_NAME).equals(DLFileEntry.class.getName())) {
                    key = "fileEntryTypeId";
                }

                document.addKeyword("type", key);
                String keyValue = document.get(key);
                if (!results.containsKey(keyValue)) {
                    List<Document> list = new ArrayList<Document>();
                    list.add(document);
                    results.put(keyValue, list);
                } else if (results.get(keyValue).size() < maxCount) {
                    results.get(keyValue).add(document);
                }
            }
        } else {
            results = Collections.emptyMap();
        }

        return results;
    }

    @Override
    public List<SearchFacet> getEnabledSearchFacets(PortletPreferences preferences) {
        return SearchFacetTracker.getSearchFacets()
                .stream()
                .filter(facet -> this.isDisplayFacet(facet, preferences))
                .collect(Collectors.toList());
    }

    private Hits customSearch(SearchContext searchContext, PortletPreferences portletPreferences) {
        // Adding the facets
        Facet assetEntriesFacet = new AssetEntriesFacet(searchContext);
        assetEntriesFacet.setStatic(true);
        searchContext.addFacet(assetEntriesFacet);

        Facet scopeFacet = new ScopeFacet(searchContext);
        scopeFacet.setStatic(true);
        searchContext.addFacet(scopeFacet);

        for (SearchFacet searchFacet : this.getEnabledSearchFacets(portletPreferences)) {
            try {
                String searchConfiguration = portletPreferences.getValue("searchConfiguration", StringPool.BLANK);
                searchFacet.init(searchContext.getCompanyId(), searchConfiguration, searchContext);
            } catch (Exception e) {
                LOG.warn("cannot init the facet " + searchFacet.getClass().getName(), e);
            }
            Facet facet = searchFacet.getFacet();
            if (facet != null) {
                searchContext.addFacet(facet);
            }
        }

        /*
         * actual search
         */
        String[] assetEntries = portletPreferences.getValues("selectedAssets", null);
        if (assetEntries != null && searchContext.getEntryClassNames().length > 1) {
            searchContext.setEntryClassNames(assetEntries);
        }

        FacetedSearcherManager facetedSearcherManager = FacetedSearcherManagerUtil.getFacetedSearcherManager();
        FacetedSearcher facetedSearcher = facetedSearcherManager.createFacetedSearcher();

        Hits hits = null;
        try {
            hits = facetedSearcher.search(searchContext);
        } catch (SearchException e) {
            LOG.error(e);
        }

        return hits;
    }

    private boolean isDisplayFacet(SearchFacet facet, PortletPreferences preferences) {
        return GetterUtil.getBoolean(preferences.getValue(facet.getClassName(), null), true);
    }

    @Reference(unbind = "-")
    public void setJournalArticleService(JournalArticleLocalService service) {
        this.journalArticleLocalService = service;
    }

}
