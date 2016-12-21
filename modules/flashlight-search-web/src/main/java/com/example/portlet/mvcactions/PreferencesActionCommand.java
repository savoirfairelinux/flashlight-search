package com.example.portlet.mvcactions;

import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.util.GetterUtil;
import com.savoirfairelinux.portlet.SearchPortletKeys;

@Component(
	    immediate = true,
	    property = {
	        "javax.portlet.name="+SearchPortletKeys.NAME,
	        "mvc.command.name=configurationURL"
	    },
	    service = MVCActionCommand.class
	    )
public class PreferencesActionCommand extends BaseMVCActionCommand{

	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		String[] displayStyle= actionRequest.getParameterValues("preferences--displayStyle--");
		String[] displayStyleGroupId = actionRequest.getParameterValues("preferences--displayStyleGroupId--");
		/*String[] valuesDisplay = {displayStyle};
		String[] valuesDisplayId = {""+displayStyleGroupId};*/
		actionRequest.getPreferences().setValues("displayStyle", displayStyle);
		actionRequest.getPreferences().setValues("displayStyleGroupId", displayStyleGroupId);
		actionRequest.getPreferences().store();
		actionRequest.getPreferences().reset("mypram");
		actionRequest.getPreferences().reset("mypram");
		actionRequest.getPreferences().reset("mypram");
		System.out.println("----------------");
	       Map<String, String[]> map = actionRequest.getPreferences().getMap();
			for(String key : map.keySet()){
				System.out.println("key : "+ key);
				for(String value : map.get(key) ){
					System.out.println("values are  : " +value);
				}
			}
			System.out.println("----------------");
		
		
	}

}
