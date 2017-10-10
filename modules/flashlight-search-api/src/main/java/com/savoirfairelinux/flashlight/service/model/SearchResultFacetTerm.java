// Copyright 2017 Savoir-faire Linux
// This file is part of Flashlight Search.

// Flashlight Search is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Flashlight Search is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Flashlight Search.  If not, see <http://www.gnu.org/licenses/>.

package com.savoirfairelinux.flashlight.service.model;

import java.util.Map;
import java.util.stream.Collectors;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.search.web.facet.SearchFacet;

public class SearchResultFacetTerm {
    private final int frequency;
    private final String term;
    private final boolean applied;
    private final Map<String, String> urlTerms;

    public SearchResultFacetTerm(SearchFacet searchFacet, TermCollector termCollector) {
        this.frequency = termCollector.getFrequency();
        this.term = termCollector.getTerm();
        this.applied = termCollector.getTerm().equals(searchFacet.getFacet().getSearchContext().getAttribute(searchFacet.getFieldName()));

        // build a map from all current facet parameters
        this.urlTerms = searchFacet.getFacet().getSearchContext().getAttributes().entrySet().stream()
            // filter out parameters that are not facets
            .filter(field -> searchFacet.getFacet().getSearchContext().getFacets().containsKey(field.getKey()))
            // filter out the current facet
            .filter(field -> !field.getKey().equals(searchFacet.getFieldName()))
            .collect(Collectors.toMap(Map.Entry::getKey, field -> field.getValue().toString()));

        this.urlTerms.put(searchFacet.getFieldName(), this.term);
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

    public Map<String, String> getUrlTerms() {
        return urlTerms;
    }
}
