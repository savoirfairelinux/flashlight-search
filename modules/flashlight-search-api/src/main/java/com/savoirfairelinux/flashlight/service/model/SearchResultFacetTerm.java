package com.savoirfairelinux.flashlight.service.model;

import com.liferay.portal.kernel.search.facet.collector.TermCollector;

public class SearchResultFacetTerm {
    private final int frequency;
    private final String term;

    public SearchResultFacetTerm(TermCollector termCollector) {
        this.frequency = termCollector.getFrequency();
        this.term = termCollector.getTerm();
    }

    public int getFrequency() {
        return frequency;
    }

    public String getTerm() {
        return term;
    }
}
