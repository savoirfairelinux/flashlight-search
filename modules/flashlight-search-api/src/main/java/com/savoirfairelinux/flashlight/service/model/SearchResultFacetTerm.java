package com.savoirfairelinux.flashlight.service.model;

import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.search.web.facet.SearchFacet;

public class SearchResultFacetTerm {
    private final int frequency;
    private final String term;
    private final boolean applied;

    public SearchResultFacetTerm(SearchFacet searchFacet, TermCollector termCollector) {
        this.frequency = termCollector.getFrequency();
        this.term = termCollector.getTerm();
        this.applied = termCollector.getTerm().equals(searchFacet.getFacet().getSearchContext().getAttribute(searchFacet.getFieldName()));
    }

    public int getFrequency() {
        return frequency;
    }

    public String getTerm() {
        return term;
    }

    public boolean isApplied() {
        return applied;
    }
}
