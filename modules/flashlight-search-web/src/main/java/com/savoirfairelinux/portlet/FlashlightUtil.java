package com.savoirfairelinux.portlet;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

public class FlashlightUtil {

	public static String getAssetViewURL(
			RenderRequest renderRequest, RenderResponse renderResponse, Document document)
		throws Exception {

		String className = document.get("entryClassName");
		int classPK = GetterUtil.getInteger(document.get("entryClassPK"));
		try {
			String currentURL = PortalUtil.getCurrentURL(renderRequest);
			ThemeDisplay themeDisplay =(ThemeDisplay)renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
			
//			PortletURL viewContentURL = renderResponse.createRenderURL();
			PortletURL viewContentURL = PortletURLFactoryUtil.create(renderRequest, "com_liferay_portal_search_web_portlet_SearchPortlet", themeDisplay.getLayout(),
					PortletRequest.RENDER_PHASE);

			viewContentURL.setParameter("mvcPath", "/view_content.jsp");
			viewContentURL.setParameter("redirect", currentURL);
			viewContentURL.setPortletMode(PortletMode.VIEW);
			viewContentURL.setWindowState(WindowState.MAXIMIZED);

			if (Validator.isNull(className) || (classPK <= 0)) {
				return viewContentURL.toString();
			}

			AssetEntry assetEntry = AssetEntryLocalServiceUtil.getEntry(className, classPK);

			AssetRendererFactory<?> assetRendererFactory = 
					AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(className);

			if (assetRendererFactory == null) {
				return viewContentURL.toString();
			}

			viewContentURL.setParameter("assetEntryId", String.valueOf(assetEntry.getEntryId()));
			viewContentURL.setParameter("type", assetRendererFactory.getType());

			AssetRenderer<?> assetRenderer = assetRendererFactory.getAssetRenderer(classPK);

			String viewURL = assetRenderer.getURLViewInContext(
				(LiferayPortletRequest)renderRequest,
				(LiferayPortletResponse)renderResponse,
				viewContentURL.toString());


			return checkViewURL(assetEntry, true, viewURL, currentURL, themeDisplay);
		}
		catch (Exception e) {
			LOG.error(
				"Unable to get search result  view URL for class " + className +
					" with primary key " + classPK, e);

			return "";
		}
	}


	public static String checkViewURL(
		AssetEntry assetEntry, boolean viewInContext, String viewURL,
		String currentURL, ThemeDisplay themeDisplay) {

		if (Validator.isNull(viewURL)) {
			return viewURL;
		}

		viewURL = HttpUtil.setParameter(
			viewURL, "inheritRedirect", viewInContext);

		Layout layout = themeDisplay.getLayout();

		String assetEntryLayoutUuid = assetEntry.getLayoutUuid();

		if (!viewInContext ||
			(Validator.isNotNull(assetEntryLayoutUuid) &&
			 !assetEntryLayoutUuid.equals(layout.getUuid()))) {

			viewURL = HttpUtil.setParameter(viewURL, "redirect", currentURL);
		}

		return viewURL;
	}
	
	private static final Log LOG = LogFactoryUtil.getLog(FlashlightUtil.class);

}