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
