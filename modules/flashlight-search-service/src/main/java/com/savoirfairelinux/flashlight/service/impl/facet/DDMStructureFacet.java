package com.savoirfairelinux.flashlight.service.impl.facet;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.savoirfairelinux.flashlight.service.impl.DocumentField;

/**
 * This facet filters document by DDM structure key
 */
public class DDMStructureFacet extends MultiValueFacet {

    public static final String CONFIG_FIELD_VALUES = "values";

    /**
     * Creates the facet with the given search context. Sets the field name to "ddmStructureKey" and makes the facet
     * static.
     *
     * @param searchContext The search context
     */
    public DDMStructureFacet(SearchContext searchContext) {
        super(searchContext);
        this.setFieldName(DocumentField.DDM_STRUCTURE_KEY.getName());
        this.setStatic(true);
    }

// TODO: Kept here while we develop, should be removed if unused later
//    @Override
//    protected BooleanClause<Filter> doGetFacetFilterBooleanClause() {
//        boolean isStatic = this.isStatic();
//        SearchContext ctx = this.getSearchContext();
//        FacetConfiguration config = this.getFacetConfiguration();
//        JSONObject configJSON = config.getData();
//        Serializable ctxValueObject = ctx.getAttribute(this.getFieldId());
//
//        String[] values;
//        if(isStatic && configJSON.has(CONFIG_FIELD_VALUES)) {
//            JSONArray configValuesJSONArray = configJSON.getJSONArray(CONFIG_FIELD_VALUES);
//            int valuesLength = configValuesJSONArray.length();
//            values = new String[valuesLength];
//
//            for(int i = 0; i < valuesLength; i++) {
//                values[i] = configValuesJSONArray.getString(i);
//            }
//        } else if(!isStatic && ctxValueObject != null) {
//            values = ctxValueObject.toString().split(StringPool.COMMA);
//        } else {
//            values = StringPool.EMPTY_ARRAY;
//        }
//
//        BooleanClause<Filter> booleanClause;
//        TermsFilter facetTermsFilter = new TermsFilter(this.getFieldName());
//        long userId = ctx.getUserId();
//        int valuesLength = values.length;
//
//        FacetValueValidator facetValueValidator = this.getFacetValueValidator();
//
//        for(int i = 0; i< valuesLength; i++) {
//            if(userId < 0 || facetValueValidator.check(ctx, values[i])) {
//                facetTermsFilter.addValue(values[i]);
//            }
//        }
//
//        if(!facetTermsFilter.isEmpty()) {
//            booleanClause = BooleanClauseFactoryUtil.createFilter(ctx, facetTermsFilter, BooleanClauseOccur.MUST);
//        } else {
//            booleanClause = null;
//        }
//
//
//        return booleanClause;
//
//    }

}
