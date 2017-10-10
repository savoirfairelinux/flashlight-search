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

package com.savoirfairelinux.flashlight.service.impl.search.result;

import com.savoirfairelinux.flashlight.service.search.result.SearchResultProcessor;

/**
 * Attaches a rank to a search result processor
 */
public class RankedSearchResultProcessor {

    private int ranking;
    private SearchResultProcessor processor;

    /**
     * Creates a ranked search result processor
     *
     * @param ranking The processor's ranking
     * @param processor The processor itself
     */
    public RankedSearchResultProcessor(int ranking, SearchResultProcessor processor) {
        this.ranking = ranking;
        this.processor = processor;
    }

    /**
     * @return The processor's ranking
     */
    public int getRanking() {
        return this.ranking;
    }

    /**
     * @return The processor itself
     */
    public SearchResultProcessor getProcessor() {
        return this.processor;
    }

}
