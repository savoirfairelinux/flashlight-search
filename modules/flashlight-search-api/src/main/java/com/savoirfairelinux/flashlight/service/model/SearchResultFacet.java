package com.savoirfairelinux.flashlight.service.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.search.web.facet.SearchFacet;

public class SearchResultFacet {

    private final String fieldName;
    private final Class<? extends SearchFacet> searchFacetClass;
    private final String label;
    private final FacetConfiguration facetConfiguration;
    private final List<SearchResultFacetTerm> terms;

    public SearchResultFacet(SearchFacet searchFacet) {
        this.fieldName = searchFacet.getFieldName();
        this.searchFacetClass = searchFacet.getClass();
        this.label = searchFacet.getLabel();
        this.facetConfiguration = searchFacet.getFacetConfiguration();
        if (searchFacet.getFacet() != null
            && searchFacet.getFacet().getFacetCollector() != null
            && searchFacet.getFacet().getFacetCollector().getTermCollectors() != null) {
            this.terms = searchFacet.getFacet().getFacetCollector().getTermCollectors().stream()
                .map(termCollector -> new SearchResultFacetTerm(searchFacet, termCollector))
                .collect(Collectors.toList());
        } else {
            this.terms = Collections.emptyList();
        }
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<? extends SearchFacet> getSearchFacetClass() {
        return searchFacetClass;
    }

    public String getLabel() {
        return label;
    }

    public FacetConfiguration getFacetConfiguration() {
        return facetConfiguration;
    }

    public List<SearchResultFacetTerm> getTerms() {
        return terms;
    }
}
