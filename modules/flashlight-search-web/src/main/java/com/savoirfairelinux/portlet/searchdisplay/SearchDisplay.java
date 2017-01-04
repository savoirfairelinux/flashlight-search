package com.savoirfairelinux.portlet.searchdisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.FacetedSearcher;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.GroupBy;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.ScopeFacet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PredicateFilter;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;

public class SearchDisplay {
	
	public SearchDisplay(PortletPreferences prefs){
		this._portletPreferences = prefs;
	}

	public Hits customSearch(RenderRequest renderRequest, String keywords, PortletPreferences portletPreferences) {

		HttpServletRequest request = PortalUtil.getHttpServletRequest(renderRequest);
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		_portletPreferences = portletPreferences;
		SearchContext searchContext = SearchContextFactory.getInstance(request);
		searchContext.setKeywords(keywords);

		/*
		 * adding the facts
		 */

		Facet assetEntriesFacet = new AssetEntriesFacet(searchContext);

		assetEntriesFacet.setStatic(true);

		searchContext.addFacet(assetEntriesFacet);

		Facet scopeFacet = new ScopeFacet(searchContext);

		scopeFacet.setStatic(true);

		searchContext.addFacet(scopeFacet);
		for (SearchFacet searchFacet : getEnabledSearchFacets()) {

			try {
				searchFacet.init(themeDisplay.getCompanyId(), getSearchConfiguration(), searchContext);
			} catch (Exception e) {
				System.out.println("cannot init the facet");// e.printStackTrace();
			}
			Facet facet = searchFacet.getFacet();

			if (facet == null) {
				continue;
			}

			searchContext.addFacet(facet);

		}


		/*
		 * Map<String, Serializable> values= new HashMap<>(); String[] val =
		 * {"31400" , "31404"}; values.put("ddmStructureKey", val);
		 * searchContext.setAttributes(values);
		 * System.out.println("number of enabled facets : "+
		 * searchContext.getFacets().size());
		 */
		/*
		 * actual search
		 */
		String selected_facet = renderRequest.getParameter("selected_facet");
		if (selected_facet != null && !selected_facet.equals("")) {

			String[] entries = { selected_facet };
			searchContext.setEntryClassNames(entries);
			System.out.println("number of entries : " + searchContext.getEntryClassNames().length);
		} else {
			searchContext.setEntryClassNames(getEntryClassNames());
			System.out.println("number of entries : " + searchContext.getEntryClassNames().length);
		}

		Indexer<?> indexer = FacetedSearcher.getInstance();
		Hits hits;
		try {
			hits = indexer.search(searchContext);

		} catch (SearchException e) {
			hits = null;
			e.printStackTrace();
		}
		return hits;
	}

	public List<Document> search(RenderRequest renderRequest, String keywords, PortletPreferences portletPreferences) {
		return customSearch(renderRequest, keywords, portletPreferences).toList();
	}

	public Map<String, List<Document>> groupedsearch(RenderRequest renderRequest, String keywords,
			PortletPreferences portletPreferences) {
		HttpServletRequest request = PortalUtil.getHttpServletRequest(renderRequest);
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		_portletPreferences = portletPreferences;
		SearchContext searchContext = SearchContextFactory.getInstance(request);
		searchContext.setKeywords(keywords);

		/*
		 * adding the facts
		 */

		Facet assetEntriesFacet = new AssetEntriesFacet(searchContext);

		assetEntriesFacet.setStatic(true);

		searchContext.addFacet(assetEntriesFacet);

		Facet scopeFacet = new ScopeFacet(searchContext);

		scopeFacet.setStatic(true);

		searchContext.addFacet(scopeFacet);
		for (SearchFacet searchFacet : getEnabledSearchFacets()) {

			try {
				searchFacet.init(themeDisplay.getCompanyId(), getSearchConfiguration(), searchContext);
			} catch (Exception e) {
				System.out.println("cannot init the facet");// e.printStackTrace();
			}
			Facet facet = searchFacet.getFacet();

			if (facet == null) {
				continue;
			}

			searchContext.addFacet(facet);
		}
		String[] entryClassNames = { "com.liferay.blogs.kernel.model.BlogsEntry",
				"com.liferay.journal.model.JournalArticle", "com.liferay.bookmarks.model.BookmarksEntry" };
		searchContext.setEntryClassNames(entryClassNames);
		GroupBy groupby = new GroupBy(Field.ENTRY_CLASS_NAME);
		searchContext.setGroupBy(groupby);

		Indexer<?> indexer = FacetedSearcher.getInstance();
		Map<String, List<Document>> results = new HashMap<String, List<Document>>();
		try {
			Hits hits = indexer.search(searchContext);
			if (hits.hasGroupedHits()) {
				for (Map.Entry<String, Hits> entry : hits.getGroupedHits().entrySet()) {
					Hits hit = entry.getValue();
					System.out.println("hit size : " + hit.getLength());
					List<Document> documents = hit.toList();
					System.out.println("title " + documents.get(0).get(Field.ENTRY_CLASS_NAME));
					results.put(entry.getKey(), documents);
				}

			}
		} catch (SearchException e) {
			e.printStackTrace();
		}
		return results;
	}

	public Map<String, List<Document>> customGroupedSearch(RenderRequest renderRequest, String keywords,
			PortletPreferences portletPreferences, String groupBy, int maxcount) {
		int size = maxcount;
		Document[] documents = customSearch(renderRequest, keywords, portletPreferences).getDocs();
		System.out.println("number of Documents: " + documents.length);
		
		
		HashMap<String, List<Document>> hashMap = new HashMap<String, List<Document>>();
		for (Document document : documents) {
			String key = groupBy;
			if (document.get(Field.ENTRY_CLASS_NAME).equals(JournalArticle.class.getName())) {
				key = "ddmStructureKey";
			} else if (document.get(Field.ENTRY_CLASS_NAME).equals(DLFileEntry.class.getName())) {
				key = "fileEntryTypeId";
				DLFileEntry file =null;
				try {
					 file = DLFileEntryLocalServiceUtil.getDLFileEntry(Long.parseLong(document.get(Field.ENTRY_CLASS_PK)));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PortalException e) {
					
					e.printStackTrace();
				}
				if(file != null){
					String imageURL = "http://localhost:8080/documents/"+file.getGroupId()+"/"+file.getFolderId()+"/"+file.getFileName()+"/"+file.getUuid()+"?version"+file.getVersion()+"&amp;documentThumbnail=1";
				//document.addKeyword("imageURL", "http://localhost:8080/documents/20147/0/web_framework.pdf/dd3f7dfd-84c7-1553-7bf8-ba6893a4ef5f?version=1.0&amp;documentThumbnail=1");
					document.addKeyword("imageURL", imageURL);
				}
			}

			if (!hashMap.containsKey(document.get(key))) {
				List<Document> list = new ArrayList<Document>();
				list.add(document);

				hashMap.put(document.get(key), list);
			} else {
				if (hashMap.get(document.get(key)).size() < size) {
					hashMap.get(document.get(key)).add(document);
				}
			}

		}
		System.out.println("size of the grouped docs map : " + hashMap.size());
		return hashMap;
	}

	public Map<String, List<Document>> customGroupedSearch(RenderRequest renderRequest, String keywords,
			PortletPreferences portletPreferences, String groupBy) {
		return customGroupedSearch(renderRequest, keywords, portletPreferences, groupBy, 8000);
	}

	public List<SearchFacet> getEnabledSearchFacets() {
		if (_enabledSearchFacets != null) {
			return _enabledSearchFacets;
		}

		_enabledSearchFacets = ListUtil.filter(SearchFacetTracker.getSearchFacets(),
				new PredicateFilter<SearchFacet>() {

					@Override
					public boolean filter(SearchFacet searchFacet) {
						return isDisplayFacet(searchFacet.getClassName());
					}

				});

		return _enabledSearchFacets;
	}

	public String getSearchConfiguration() {
		if (_searchConfiguration != null) {
			return _searchConfiguration;
		}

		_searchConfiguration = _portletPreferences.getValue("searchConfiguration", StringPool.BLANK);

		return _searchConfiguration;
	}

	public boolean isDisplayFacet(String className) {
		return GetterUtil.getBoolean(_portletPreferences.getValue(className, null), true);
	}

	public static String[] getEntryClassNames() {
		return entryClassNames;
	}

	public final static String[] entryClassNames = { // "com.liferay.blogs.kernel.model.BlogsEntry",
			"com.liferay.document.library.kernel.model.DLFileEntry",
			// "com.liferay.bookmarks.model.BookmarksEntry",
			"com.liferay.journal.model.JournalArticle",
			// "com.liferay.portal.kernel.model.User"
	};

	public void setPortletPreferences(PortletPreferences prefs) {
		_portletPreferences = prefs;
	}

	private String _searchConfiguration;
	private List<SearchFacet> _enabledSearchFacets;
	private PortletPreferences _portletPreferences;

}