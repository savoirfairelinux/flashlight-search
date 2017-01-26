package com.savoirfairelinux.portlet.mvcactions;

import java.util.Enumeration;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.savoirfairelinux.portlet.SearchPortletKeys;

@Component(immediate = true, property = { 
        "javax.portlet.name=" + SearchPortletKeys.NAME,
        "mvc.command.name=configurationURL" }, 
        service = MVCActionCommand.class)
public class PreferencesActionCommand extends BaseMVCActionCommand {

    @Override
    protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
        String[] displayStyle = actionRequest.getParameterValues("preferences--displayStyle--");
        String[] displayStyleGroupId = actionRequest.getParameterValues("preferences--displayStyleGroupId--");
        String[] facets = actionRequest.getParameterValues("selected_facets");
        String[] selectAssets = actionRequest.getParameterValues("selected_assets_entries");

        Enumeration<String> e = actionRequest.getParameterNames();
        while (e.hasMoreElements()) {
            String param = (String) e.nextElement();
            if (param.startsWith("ddm-")) {
                actionRequest.getPreferences().setValue(param, actionRequest.getParameter(param));
            }

        }
        if (facets == null) {
            facets = new String[0];
        }
        actionRequest.getPreferences().setValues("facets", facets);
        actionRequest.getPreferences().setValues("displayStyle", displayStyle);
        actionRequest.getPreferences().setValues("displayStyleGroupId", displayStyleGroupId);
        actionRequest.getPreferences().setValues("selectedAssets", selectAssets);
        actionRequest.getPreferences().store();

    }

}
