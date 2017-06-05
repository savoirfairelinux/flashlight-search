package com.savoirfairelinux.flashlight.service.impl.search.result;

import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.savoirfairelinux.flashlight.service.search.result.SearchResultProcessor;

/**
 * This is the tracker customizer that manages the search result processors
 */
public class SearchResultProcessorServiceTrackerCustomizer implements ServiceTrackerCustomizer<SearchResultProcessor, SearchResultProcessor> {

    private static final Log LOG = LogFactoryUtil.getLog(SearchResultProcessorServiceTrackerCustomizer.class);

    private Map<String, RankedSearchResultProcessor> processors;
    private BundleContext bundleContext;

    /**
     * Creates the tracker customizer
     *
     * @param bundleContext The bundle context in which the customizer operates
     * @param processors A concurrent implementation of a map that is used to map processors to asset types
     */
    public SearchResultProcessorServiceTrackerCustomizer(BundleContext bundleContext, Map<String, RankedSearchResultProcessor> processors) {
        this.bundleContext = bundleContext;
        this.processors = processors;
    }

    @Override
    public SearchResultProcessor addingService(ServiceReference<SearchResultProcessor> reference) {
        Object serviceRankingObj = reference.getProperty(Constants.SERVICE_RANKING);
        SearchResultProcessor service = this.bundleContext.getService(reference);

        int serviceRanking;
        if(serviceRankingObj != null && serviceRankingObj instanceof Integer) {
            serviceRanking = (int) serviceRankingObj;
        } else {
            LOG.info("Invalid service ranking (not an integer). Was given : \"" + String.valueOf(serviceRankingObj) + "\". Defaulting to rank 0.");
            serviceRanking = 0;
        }

        String assetType = service.getAssetType();
        RankedSearchResultProcessor mapping = this.processors.get(assetType);
        if(mapping == null || mapping.getRanking() < serviceRanking) {
            this.processors.put(assetType, new RankedSearchResultProcessor(serviceRanking, service));
        }


        return service;
    }

    @Override
    public void modifiedService(ServiceReference<SearchResultProcessor> reference, SearchResultProcessor service) {}

    @Override
    public void removedService(ServiceReference<SearchResultProcessor> reference, SearchResultProcessor service) {
        this.processors.remove(service.getAssetType());
    }

}
