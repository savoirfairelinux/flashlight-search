package com.example.facet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.web.facet.BaseJSPSearchFacet;
import com.liferay.portal.search.web.facet.SearchFacet;

@Component(immediate = true, service = SearchFacet.class)
public class StrucutreFacet extends BaseJSPSearchFacet {

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
		String className = getClassName();
		System.out.println("Strcuture facet class name : " + className);
		return className;
	}

	@Override
	public String getFieldName() {
		String field = "ddmStructureKey";
		return field;
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
		String label = "Structure facet";
		return label;
	}

	/*@Override
	public void includeConfiguration(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.getRequestDispatcher("/facet/config/structure.jsp");
		
	}

	@Override
	public void includeView(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.getRequestDispatcher("/facet/structure.jsp");
		
	}*/
	
	@Override
	public String getTitle() {
		return "Structure";
	}
	
	@Override
	@Reference(
		target = "(osgi.web.symbolicname=flashlight-search)",
		unbind = "-"
	)
	public void setServletContext(ServletContext servletContext) {
		super.setServletContext(servletContext);
	}
	
	private Facet _facet;
	private FacetConfiguration _facetConfiguration;
	@Override
	public String getConfigurationJspPath() {
		
		return "/facet/config/structure.jsp";
	}

	@Override
	public String getDisplayJspPath() {
		// TODO Auto-generated method stub
		return "/facet/structure.ftl";
	}
}
