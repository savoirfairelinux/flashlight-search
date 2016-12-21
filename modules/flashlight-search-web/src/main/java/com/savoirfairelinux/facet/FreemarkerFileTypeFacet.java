package com.savoirfairelinux.facet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.web.facet.BaseSearchFacet;
import com.liferay.portal.search.web.facet.SearchFacet;

@Component(immediate = true, service = SearchFacet.class)
public class FreemarkerFileTypeFacet extends BaseSearchFacet {
	
	@Override
	public FacetConfiguration getDefaultConfiguration(long companyId) {
		FacetConfiguration facetConfiguration = new FacetConfiguration();

		facetConfiguration.setClassName(getFacetClassName());

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		jsonObject.put("displayStyle", "list");
		jsonObject.put("frequencyThreshold", 1);
		jsonObject.put("maxTerms", 10);
		jsonObject.put("showAssetCount", true);

		facetConfiguration.setDataJSONObject(jsonObject);

		facetConfiguration.setFieldName(getFieldName());
		facetConfiguration.setLabel(getLabel());
		facetConfiguration.setOrder(getOrder());
		facetConfiguration.setStatic(false);
		facetConfiguration.setWeight(1.3);

		return facetConfiguration;
	}

	@Override
	public String getFacetClassName() {
		return MultiValueFacet.class.getName();
	}

	@Override
	public String getFieldName() {
		return "fileEntryTypeId";
	}

	@Override
	public JSONObject getJSONData(ActionRequest actionRequest) {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		String displayStyleFacet = ParamUtil.getString(
			actionRequest, getClassName() + "displayStyleFacet", "list");
		int frequencyThreshold = ParamUtil.getInteger(
			actionRequest, getClassName() + "frequencyThreshold", 1);
		int maxTerms = ParamUtil.getInteger(
			actionRequest, getClassName() + "maxTerms", 10);
		boolean showAssetCount = ParamUtil.getBoolean(
			actionRequest, getClassName() + "showAssetCount", true);

		jsonObject.put("displayStyle", displayStyleFacet);
		jsonObject.put("frequencyThreshold", frequencyThreshold);
		jsonObject.put("maxTerms", maxTerms);
		jsonObject.put("showAssetCount", showAssetCount);

		return jsonObject;
	}

	@Override
	public String getLabel() {
		return "Structure facet";
	}

	@Override
	public void includeConfiguration(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (Validator.isNull(getConfigurationPath())) {
			return;
		}

		RequestDispatcher requestDispatcher =
			_servletContext.getRequestDispatcher(getConfigurationPath());

		try {
			requestDispatcher.include(request, response);
		}
		catch (ServletException se) {
			_log.error("Unable to include FTL " + getConfigurationPath(), se);

			throw new IOException(
				"Unable to include " + getConfigurationPath(), se);
		}
		
	}

	@Override
	public void includeView(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (Validator.isNull(getDisplayPath())) {
			return;
		}

		RequestDispatcher requestDispatcher =
			_servletContext.getRequestDispatcher(getDisplayPath());

		try {
			requestDispatcher.include(request, response);
		}
		catch (ServletException se) {
			_log.error("Unable to include FTL", se);

			throw new IOException(
				"Unable to include " + getDisplayPath(), se);
		}
	}
	
	public String getDisplayPath(){
		//not used yet
		return "/facet/structure.ftl";
	}
	public String getConfigurationPath(){
		//not used yet
		return "/facet/config/structure.ftl";
	}
	
	
	@Reference(
		target = "(osgi.web.symbolicname=com.savoirfairelinux.liferay.flashlight-search-web)",
		unbind = "-"
	)
	public void setServletContext(ServletContext servletContext) {
		_servletContext = servletContext;
	}
	
	private ServletContext _servletContext;
	private static final Log _log = LogFactoryUtil.getLog(
			FreemarkerStructureFacet.class);
}
