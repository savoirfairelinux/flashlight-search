package com.savoirfairelinux.flashlight.action;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.savoirfairelinux.flashlight.service.SearchService;

@Component(service = LifecycleAction.class, property = { "key=servlet.service.events.pre" })
public class SearchUrlAction extends Action {

    private static final Log LOG = LogFactoryUtil.getLog(SearchUrlAction.class);
    private static final String FLASHLIGHT_MVC_PATH = "&_%s_mvcPath=search_result.ftl&";
    private static final String FLASHLIGHT_KEYWORDS_PARAM = "_%s_keywords=";

    private static final String OUTPUT_FLASHLIGHT_URL = "flashlight-url";
    private static final String OUTPUT_FLASHLIGHT_KEYWORD_PARAMETER = "flashlight-keyword-parameter";

    @Override
    public void run(HttpServletRequest p_Request, HttpServletResponse p_Response) throws ActionException {
        Layout currentLayout = getCurrentLayoutFromRequest(p_Request);
        PortletURL flashlightPortlet = PortletURLFactoryUtil.create(p_Request, SearchService.PORTLET_NAME, currentLayout,
                PortletRequest.RENDER_PHASE);

        if (flashlightPortlet != null) {
            String flashlightUrl = createFlashlightSearchUrl(flashlightPortlet.toString());
            p_Request.setAttribute(OUTPUT_FLASHLIGHT_URL, flashlightUrl);
            p_Request.setAttribute(OUTPUT_FLASHLIGHT_KEYWORD_PARAMETER,
                    String.format(FLASHLIGHT_KEYWORDS_PARAM, SearchService.PORTLET_NAME));
        } else {
            LOG.warn("Flashlight portlet not detected.");
        }
    }

    private Layout getCurrentLayoutFromRequest(HttpServletRequest p_Request) {
        ThemeDisplay themeDisplay = (ThemeDisplay) p_Request.getAttribute(WebKeys.THEME_DISPLAY);
        return themeDisplay.getLayout();
    }

    private String createFlashlightSearchUrl(String p_FlashlightPortletUrl) {
        StringBuilder flashlightUrlBuilder = new StringBuilder(p_FlashlightPortletUrl);
        flashlightUrlBuilder.append(String.format(FLASHLIGHT_MVC_PATH, SearchService.PORTLET_NAME));
        return flashlightUrlBuilder.toString();
    }
}
