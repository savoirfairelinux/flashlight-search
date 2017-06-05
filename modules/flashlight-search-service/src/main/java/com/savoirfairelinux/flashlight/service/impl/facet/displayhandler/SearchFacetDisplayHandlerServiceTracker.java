package com.savoirfairelinux.flashlight.service.impl.facet.displayhandler;

import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.savoirfairelinux.flashlight.service.facet.SearchFacetDisplayHandler;

/**
 * Tracker for SearchFacetDisplayHandler, used to format facets as displayed in the results page.
 *
 * @see SearchFacetDisplayHandler#displayTerm(java.util.Locale, com.liferay.portal.search.web.facet.SearchFacet, java.lang.String)
 */
@Component(immediate = true, service = SearchFacetDisplayHandlerServiceTracker.class)
public class SearchFacetDisplayHandlerServiceTracker implements ServiceTrackerCustomizer<SearchFacetDisplayHandler, SearchFacetDisplayHandler> {

    private BundleContext bundleContext;

    private ServiceTracker<SearchFacetDisplayHandler, SearchFacetDisplayHandler> searchFacetDisplayHandlerServiceTracker;
    private Map<String, SearchFacetDisplayHandler> displayHandlers;

    @Activate
    public void init(BundleContext ctx) {
        this.bundleContext = ctx;
        this.displayHandlers = new HashMap<>();
        this.searchFacetDisplayHandlerServiceTracker = new ServiceTracker<>(ctx, SearchFacetDisplayHandler.class, this);
        this.searchFacetDisplayHandlerServiceTracker.open();
    }

    @Deactivate
    public void destroy() {
        this.searchFacetDisplayHandlerServiceTracker.close();
    }

    @Override
    public SearchFacetDisplayHandler addingService(ServiceReference<SearchFacetDisplayHandler> reference) {
        SearchFacetDisplayHandler displayHandler = this.bundleContext.getService(reference);
        this.displayHandlers.put(displayHandler.getSearchFacetClassName(), displayHandler);
        return displayHandler;
    }

    @Override
    public void modifiedService(ServiceReference<SearchFacetDisplayHandler> reference, SearchFacetDisplayHandler service) {
        // Nada
    }

    @Override
    public void removedService(ServiceReference<SearchFacetDisplayHandler> reference, SearchFacetDisplayHandler service) {
        this.displayHandlers.remove(service.getSearchFacetClassName());
    }

    /**
     * Retrieve a search facet display handler from a com.liferay.portal.search.web.facet.SearchFacet implementation class.
     * @param searchFacetClass the implementation of com.liferay.portal.search.web.facet.SearchFacet.
     * @return the matching display handler, or null if no handlers could be found.
     */
    public SearchFacetDisplayHandler getSearchFacetDisplayHandlerBySearchFacet(Class<? extends SearchFacet> searchFacetClass) {
        return this.displayHandlers.get(searchFacetClass.getName());
    }
}
