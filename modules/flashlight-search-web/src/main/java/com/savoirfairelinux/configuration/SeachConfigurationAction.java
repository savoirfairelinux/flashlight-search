package com.savoirfairelinux.configuration;

import java.util.Map;

import javax.portlet.PortletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.savoirfairelinux.portlet.SearchPortletKeys;

import aQute.bnd.annotation.metatype.Configurable;


@Component(
	    configurationPid = "com.example.configuration.SearchConfiguration",
	    configurationPolicy = ConfigurationPolicy.OPTIONAL,
	    immediate = true,
	    property = {
	        "javax.portlet.name="+SearchPortletKeys.NAME
	    },
	    service = ConfigurationAction.class
	)
public class SeachConfigurationAction extends DefaultConfigurationAction {

	
	@Override
    public void include(
        PortletConfig portletConfig, HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse) throws Exception {

        httpServletRequest.setAttribute(
            SearchConfiguration.class.getName(),
            searchConfiguration);

        super.include(portletConfig, httpServletRequest, httpServletResponse);
    }

    @Activate
    @Modified
    protected void activate(Map<Object, Object> properties) {
        searchConfiguration = Configurable.createConfigurable(
        		SearchConfiguration.class, properties);
    }

    private volatile SearchConfiguration searchConfiguration;
}
