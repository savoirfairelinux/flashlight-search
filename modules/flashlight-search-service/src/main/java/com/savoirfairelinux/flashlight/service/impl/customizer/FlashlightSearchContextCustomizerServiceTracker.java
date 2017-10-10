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

package com.savoirfairelinux.flashlight.service.impl.customizer;

import java.util.HashSet;
import java.util.Set;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.liferay.portal.kernel.search.SearchContext;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfiguration;
import com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchConfigurationTab;
import com.savoirfairelinux.flashlight.service.customizer.FlashlightSearchContextCustomizer;

/**
 * Tracker for FlashlightSearchContextCustomizer, allowing specific SearchContext customization.
 *
 * @see FlashlightSearchContextCustomizer#customizeSearchContext
 */
@Component(immediate = true, service = FlashlightSearchContextCustomizerServiceTracker.class)
public class FlashlightSearchContextCustomizerServiceTracker implements ServiceTrackerCustomizer<FlashlightSearchContextCustomizer, FlashlightSearchContextCustomizer> {

    private BundleContext bundleContext;

    private ServiceTracker<FlashlightSearchContextCustomizer, FlashlightSearchContextCustomizer> customizerServiceTracker;
    private Set<FlashlightSearchContextCustomizer> customizers;

    @Activate
    public void init(BundleContext ctx) {
        this.bundleContext = ctx;
        this.customizers = new HashSet<>();
        this.customizerServiceTracker = new ServiceTracker<>(ctx, FlashlightSearchContextCustomizer.class, this);
        this.customizerServiceTracker.open();
    }

    @Deactivate
    public void destroy() {
        this.customizerServiceTracker.close();
    }

    @Override
    public FlashlightSearchContextCustomizer addingService(ServiceReference<FlashlightSearchContextCustomizer> reference) {
        FlashlightSearchContextCustomizer contextCustomizer = this.bundleContext.getService(reference);
        this.customizers.add(contextCustomizer);
        return contextCustomizer;
    }

    @Override
    public void modifiedService(ServiceReference<FlashlightSearchContextCustomizer> reference, FlashlightSearchContextCustomizer service) {
        // Nada
    }

    @Override
    public void removedService(ServiceReference<FlashlightSearchContextCustomizer> reference, FlashlightSearchContextCustomizer service) {
        this.customizers.remove(service);
    }

    /**
     * Call all instances of FlashlightSearchContextCustomizer services, in undetermined order.
     *
     * @param request The portlet request
     * @param response The portlet response
     * @param config The search configuration
     * @param tab The search configuration tab
     * @param searchContext the search context to customize.
     */
    public void applyCustomizers(PortletRequest request, PortletResponse response, FlashlightSearchConfiguration config, FlashlightSearchConfigurationTab tab, SearchContext searchContext) {
        this.customizers.forEach(customizer -> customizer.customizeSearchContext(request, response, config, tab, searchContext));
    }
}
