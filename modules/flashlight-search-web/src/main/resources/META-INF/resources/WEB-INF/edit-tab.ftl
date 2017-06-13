<#include "init.ftl" />

<@liferay_ui["success"] key="configuration.saved" message="Configuration saved" />

<p><a href="${editGlobalUrl}"><@liferay_ui["message"] key="Back to global configuration" /></a></p>

<form action="${saveTabUrl}" method="POST" name="${ns}tab-form">
    <#if tabId?? && tabId != "">
        <input type="hidden" name="${ns}tab-id" value="${tabId}" />
        <input type="hidden" name="${ns}edit-mode" value="tab" />
    </#if>
    <input type="hidden" name="${ns}asset-type" value="${assetType}" />
    <input type="hidden" name="${ns}redirect-url" value="${redirectUrl}" />

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="Tab information" /></legend>
        <div class="form-group input-select-wrapper">
            <label for="${ns}tab-order"><@liferay_ui["message"] key="Tab order" /></label>
            <select class="form-control" id="${ns}tab-order" name="${ns}tab-order">
                <#list 1..tabOrderRange as i>
                    <#if i == tabOrder>
                        <option value="${i}" selected="selected">${i}</option>
                    <#else>
                        <option value="${i}">${i}</option>
                    </#if>
                </#list>
            </select>
        </div>
        <#list availableLocales as availableLocale>
            <div class="form-group input-text-wrapper">
                <label for="${ns}title-${availableLocale}">${availableLocale.getDisplayName(locale)}</label>
                <input type="text" id="${ns}title-${availableLocale}" name="${ns}title-${availableLocale}" value="${titleMap[availableLocale]!''}" />
            </div>
        </#list>
        <div class="form-group input-text-wrapper">
            <label for="${ns}page-size"><@liferay_ui["message"] key="Page size" /></label>
            <input type="text" id="${ns}page-size" name="${ns}page-size" value="${tabPageSize}" />
        </div>
        <div class="form-group input-text-wrapper">
            <label for="${ns}full-page-size"><@liferay_ui["message"] key="Detailed page size" /></label>
            <input type="text" id="${ns}full-page-size" name="${ns}full-page-size" value="${tabFullPageSize}" />
        </div>
        <div class="form-group input-text-wrapper">
            <label for="${ns}load-more-page-size"><@liferay_ui["message"] key="Load more page size" /></label>
            <input type="text" id="${ns}load-more-page-size" name="${ns}load-more-page-size" value="${tabLoadMorePageSize}" />
        </div>
    </fieldset>

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="Facet configuration" /></legend>
        <div class="form-group input-checkbox-wrapper">
            <table class="table table-bordered">
                <summary><@liferay_ui["message"] key="Search facets" /></summary>
                <thead>
                    <tr><th><@liferay_ui["message"] key="Facet" /></th><th><@liferay_ui["message"] key="Action" /></th></tr>
                </thead>
                <#list supportedSearchFacets as supportedFacet>
                    <#assign facetClassName = supportedFacet.class.name />
                    <tr>
                        <td>
                            <#if searchFacets[facetClassName]??>
                                <input type="checkbox" id="${ns}search-facets-${facetClassName}" name="${ns}search-facets" checked="checked" value="${facetClassName}" />
                            <#else>
                                <input type="checkbox" id="${ns}search-facets-${facetClassName}" name="${ns}search-facets" value="${facetClassName}" />
                            </#if>
                            <label for="${ns}search-facets-${facetClassName}">${supportedFacet.title}</label>
                        </td>
                        <td>
                            <#if searchFacetUrls[facetClassName]??>
                                <a href="${searchFacetUrls[facetClassName]}"><@liferay_ui["message"] key="configure" /></a>
                            </#if>
                        </td>
                    </tr>
                </#list>
            </table>
        </div>
    </fieldset>

    <#if assetTypeEditViews[assetType]??>
        <#include assetTypeEditViews[assetType]>
    </#if>

    <div class="form-group">
        <input class="btn btn-default" type="submit" value="<@liferay_ui['message'] key='Submit' />" />
    </div>

</form>
