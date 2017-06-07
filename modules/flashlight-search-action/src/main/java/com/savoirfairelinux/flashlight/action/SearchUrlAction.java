package com.savoirfairelinux.flashlight.action;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.portlet.PortletURLFactory;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.WebKeys;
import com.savoirfairelinux.flashlight.service.FlashlightSearchPortletKeys;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.PortletMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static java.lang.String.format;

/**
 * This action is used to insert the possible search URLs in the request attributes. This way, a search box can be
 * placed in the theme, without the need to put embedded portlets.
 */
@Component(
    service = LifecycleAction.class,
    property = { "key=servlet.service.events.pre" }
)
public class SearchUrlAction extends Action {

    public static final String REQUEST_ATTR_FLASHLIGHT_URLS = FlashlightSearchPortletKeys.PORTLET_NAME + "_urls";

    private static final String FORMAT_KEYWORDS_PARAM = "%skeywords";

    private static final String PARAM_PORTLET_ID = "p_p_id";
    private static final String PARAM_PORTLET_LIFECYCLE = "p_p_lifecycle";
    private static final String PARAM_PORTLET_MODE = "p_p_mode";
    private static final String PARAM_PORTLET_COLUMN_ID = "p_p_col_id";
    private static final String PARAM_PORTLET_COLUMN_COUNT = "p_p_col_count";

    private static final String LIFECYCLE_RENDER = "0";

    private static final Log LOG = LogFactoryUtil.getLog(SearchUrlAction.class);

    @Reference
    private Portal portal;

    @Reference
    private PortletURLFactory portletUrlFactory;

    @Reference
    private LayoutLocalService layoutService;

    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) throws ActionException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        Layout currentLayout = themeDisplay.getLayout();
        String keywordsParam = format(FORMAT_KEYWORDS_PARAM, this.portal.getPortletNamespace(FlashlightSearchPortletKeys.PORTLET_NAME));

        List<Layout> searchLayouts = this.getSearchLayouts(currentLayout);
        ArrayList<SearchUrl> searchUrls = new ArrayList<>(searchLayouts.size());
        for(Layout searchLayout : searchLayouts) {
            SearchUrl url = this.generateSearchUrl(request, themeDisplay, searchLayout, keywordsParam);
            if(url != null) {
                searchUrls.add(url);
            }
        }

        request.setAttribute(REQUEST_ATTR_FLASHLIGHT_URLS, searchUrls);
    }

    /**
     * Returns a list of search URLs to be put in the request attributes
     *
     * @param request The HTTP request
     * @param themeDisplay The theme display
     * @param layout The current page
     * @param keywordsParam The name of the request parameter containing the search keywords
     * @return A list of search URLs to be put in the request attributes
     */
    public SearchUrl generateSearchUrl(HttpServletRequest request, ThemeDisplay themeDisplay, Layout layout, String keywordsParam) {
        LayoutTypePortlet layoutType = (LayoutTypePortlet) layout.getLayoutType();
        Portlet portletInstance = null;

        // Find the portlet in the page
        for(Portlet portlet : layoutType.getPortlets()) {
            if(portlet.getPortletId().equals(FlashlightSearchPortletKeys.PORTLET_NAME)) {
                portletInstance = portlet;
                break;
            }
        }

        SearchUrl searchUrl;
        if(portletInstance != null) {
            // Get information about the column from which the portlet comes from
            UnicodeProperties props = layoutType.getTypeSettingsProperties();
            String columnId = StringPool.BLANK;
            for(Entry<String, String> entry : props.entrySet()) {
                if(entry.getValue().equals(FlashlightSearchPortletKeys.PORTLET_NAME)) {
                    columnId = entry.getKey();
                    break;
                }
            }

            String portletUrl;

            try {
                portletUrl = this.portal.getLayoutFriendlyURL(layout, themeDisplay);
            } catch (PortalException e) {
                portletUrl = StringPool.BLANK;
                LOG.error(e);
            }

            RequestParameter[] params = new RequestParameter[5];
            params[0] = new RequestParameter(PARAM_PORTLET_ID, FlashlightSearchPortletKeys.PORTLET_NAME);
            params[1] = new RequestParameter(PARAM_PORTLET_LIFECYCLE, LIFECYCLE_RENDER);
            params[2] = new RequestParameter(PARAM_PORTLET_MODE, PortletMode.VIEW.toString());
            params[3] = new RequestParameter(PARAM_PORTLET_COLUMN_ID, columnId);
            params[4] = new RequestParameter(PARAM_PORTLET_COLUMN_COUNT, Integer.toString(layoutType.getNumOfColumns()));
            searchUrl = new SearchUrl(layout, portletUrl, params, keywordsParam);
        } else {
            // Somehow, the portlet is not found in the page.
            searchUrl = null;
        }

        return searchUrl;
    }

    /**
     * Returns a list of layouts that contains the search portlet. Only layouts with the same private/public status and
     * groupId as the given layout are searched for.
     *
     * @param currentLayout The layout from which to start the search.
     * @return The list of layouts that contains the search portlet
     */
    private List<Layout> getSearchLayouts(Layout currentLayout) {
        long groupId = currentLayout.getGroupId();
        boolean privateLayout = currentLayout.isPrivateLayout();
        List<Layout> siteLayouts = this.layoutService.getLayouts(groupId, privateLayout, LayoutConstants.TYPE_PORTLET);

        List<Layout> searchLayouts = new ArrayList<>();
        for(Layout layout : siteLayouts) {
            // We can safely type cast because we specify the layout type in the query above
            LayoutTypePortlet layoutType = (LayoutTypePortlet) layout.getLayoutType();
            if(layoutType.hasPortletId(FlashlightSearchPortletKeys.PORTLET_NAME)) {
                searchLayouts.add(layout);
            }
        }

        return searchLayouts;
    }

}
