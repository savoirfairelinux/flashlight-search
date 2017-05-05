package com.savoirfairelinux.flashlight.service.impl.facet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.facet.ModifiedFacet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.web.facet.BaseSearchFacet;
import com.liferay.portal.search.web.facet.SearchFacet;

@Component(immediate = true, service = SearchFacet.class)
public class CreatedSearchFacet extends BaseSearchFacet {
    private static final String[] _LABELS = new String[] { "past-hour", "past-24-hours", "past-week", "past-month",
            "past-year" };
    private static final String[] _RANGES = new String[] { "[past-hour TO *]", "[past-24-hours TO *]",
            "[past-week TO *]", "[past-month TO *]", "[past-year TO *]" };

    @Override
    public FacetConfiguration getDefaultConfiguration(long companyId) {
        FacetConfiguration facetConfiguration = new FacetConfiguration();

        facetConfiguration.setClassName(getFacetClassName());

        JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

        jsonObject.put("frequencyThreshold", 0);

        JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

        for (int i = 0; i < _LABELS.length; i++) {
            JSONObject range = JSONFactoryUtil.createJSONObject();

            range.put("label", _LABELS[i]);
            range.put("range", _RANGES[i]);

            jsonArray.put(range);
        }

        jsonObject.put("ranges", jsonArray);

        facetConfiguration.setDataJSONObject(jsonObject);
        facetConfiguration.setFieldName(getFieldName());
        facetConfiguration.setLabel(getLabel());
        facetConfiguration.setOrder(getOrder());
        facetConfiguration.setStatic(false);
        facetConfiguration.setWeight(1.0);

        return facetConfiguration;
    }

    @Override
    public String getFacetClassName() {
        return ModifiedFacet.class.getName();
    }

    @Override
    public String getFieldName() {
        return Field.CREATE_DATE;
    }

    @Override
    public JSONObject getJSONData(ActionRequest actionRequest) {
        JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

        int frequencyThreshold = ParamUtil.getInteger(actionRequest, getClassName() + "frequencyThreshold", 1);

        jsonObject.put("frequencyThreshold", frequencyThreshold);

        JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

        String[] rangesIndexes = StringUtil.split(ParamUtil.getString(actionRequest, getClassName() + "rangesIndexes"));

        for (String rangesIndex : rangesIndexes) {
            JSONObject rangeJSONObject = JSONFactoryUtil.createJSONObject();

            String label = ParamUtil.getString(actionRequest, getClassName() + "label_" + rangesIndex);
            String range = ParamUtil.getString(actionRequest, getClassName() + "range_" + rangesIndex);

            rangeJSONObject.put("label", label);
            rangeJSONObject.put("range", range);

            jsonArray.put(rangeJSONObject);
        }

        jsonObject.put("ranges", jsonArray);

        return jsonObject;
    }

    @Override
    public String getLabel() {
        return "CreatedAt";
    }

    @Override
    public void includeConfiguration(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void includeView(HttpServletRequest request, HttpServletResponse response) throws IOException {
    }

}
