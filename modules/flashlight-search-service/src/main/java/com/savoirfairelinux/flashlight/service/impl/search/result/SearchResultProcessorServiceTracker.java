package com.savoirfairelinux.flashlight.service.impl.search.result;

import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;

import com.savoirfairelinux.flashlight.service.search.result.SearchResultProcessor;

/**
 * This service tracker is used to obtain search result processors mapped to asset types
 */
@Component(immediate = true, service = SearchResultProcessorServiceTracker.class)
public class SearchResultProcessorServiceTracker {

    private ServiceTracker<SearchResultProcessor, SearchResultProcessor> serviceTracker;
    private ConcurrentHashMap<String, RankedSearchResultProcessor> searchResultProcessors;

    @Activate
    public void activate(BundleContext ctx) {
        this.searchResultProcessors = new ConcurrentHashMap<>();
        SearchResultProcessorServiceTrackerCustomizer customizer = new SearchResultProcessorServiceTrackerCustomizer(ctx, this.searchResultProcessors);
        this.serviceTracker = new ServiceTracker<>(ctx, SearchResultProcessor.class, customizer);
        this.serviceTracker.open();
    }

    /**
     * Returns a processor associated with the given asset type
     *
     * @param assetType The asset type's class name
     * @return A processor associated with the given asset type or null if none mapped
     */
    public SearchResultProcessor getSearchResultProcessor(String assetType) {
        RankedSearchResultProcessor mapping = this.searchResultProcessors.get(assetType);
        SearchResultProcessor processor;
        if(mapping != null) {
            processor = mapping.getProcessor();
        } else {
            processor = null;
        }

        return processor;
    }

    @Deactivate
    public void deactivate() {
        this.searchResultProcessors = null;
        this.serviceTracker.close();
    }

}
