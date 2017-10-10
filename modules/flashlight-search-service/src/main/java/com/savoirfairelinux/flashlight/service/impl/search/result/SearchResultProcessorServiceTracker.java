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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.savoirfairelinux.flashlight.service.search.result.SearchResultProcessor;

/**
 * This service tracker is used to obtain search result processors mapped to asset types
 */
@Component(immediate = true, service = SearchResultProcessorServiceTracker.class)
public class SearchResultProcessorServiceTracker implements ServiceTrackerCustomizer<SearchResultProcessor, SearchResultProcessor> {

    private static final Log LOG = LogFactoryUtil.getLog(SearchResultProcessorServiceTracker.class);

    private BundleContext bundleContext;
    private ServiceTracker<SearchResultProcessor, SearchResultProcessor> serviceTracker;
    private ConcurrentHashMap<String, RankedSearchResultProcessor> searchResultProcessors;

    @Activate
    public void activate(BundleContext ctx) {
        this.bundleContext = ctx;
        this.searchResultProcessors = new ConcurrentHashMap<>();
        this.serviceTracker = new ServiceTracker<>(ctx, SearchResultProcessor.class, this);
        this.serviceTracker.open();
    }

    @Override
    public SearchResultProcessor addingService(ServiceReference<SearchResultProcessor> reference) {
        Object serviceRankingObj = reference.getProperty(Constants.SERVICE_RANKING);
        SearchResultProcessor service = this.bundleContext.getService(reference);

        int serviceRanking;
        if(serviceRankingObj != null && serviceRankingObj instanceof Integer && ((Integer) serviceRankingObj).compareTo(Integer.valueOf(0)) > 0) {
            serviceRanking = (int) serviceRankingObj;
        } else {
            LOG.info("Invalid service ranking (not positive integer). Was given : \"" + String.valueOf(serviceRankingObj) + "\". Defaulting to rank 0.");
            serviceRanking = 0;
        }

        String assetType = service.getAssetType();
        RankedSearchResultProcessor mapping = this.searchResultProcessors.get(assetType);
        if(mapping == null || mapping.getRanking() < serviceRanking) {
            this.searchResultProcessors.put(assetType, new RankedSearchResultProcessor(serviceRanking, service));
        }

        return service;
    }

    @Override
    public void modifiedService(ServiceReference<SearchResultProcessor> reference, SearchResultProcessor service) {}

    @Override
    public void removedService(ServiceReference<SearchResultProcessor> reference, SearchResultProcessor service) {
        this.searchResultProcessors.remove(service.getAssetType());
    }

    /**
     * @return The list of supported asset types
     */
    public List<String> getSupportedAssetTypes() {
        return Collections.list(this.searchResultProcessors.keys());
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
