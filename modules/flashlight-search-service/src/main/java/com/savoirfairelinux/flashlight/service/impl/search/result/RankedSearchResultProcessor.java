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
