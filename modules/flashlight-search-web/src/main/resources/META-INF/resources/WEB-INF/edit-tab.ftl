<#include "init.ftl" />

<@liferay_ui["success"] key="configuration.saved" message="message.configuration.saved" />

<header class="header-toolbar">
    <div class="toolbar-group-content">
        <a href="${editGlobalUrl}" title="<@liferay_ui['message'] key='action.back.to.global.conf' />"><span class="icon-angle-left"></span> <@liferay_ui["message"] key="action.back.to.global.conf" /></a>

    </div>
</header>

<form action="${saveTabUrl}" method="POST" name="${ns}tab-form">
    <#if tabId?? && tabId != "">
        <input type="hidden" name="${ns}tab-id" value="${tabId}" />
        <input type="hidden" name="${ns}edit-mode" value="tab" />
    </#if>
    <input type="hidden" name="${ns}asset-type" value="${assetType}" />
    <input type="hidden" name="${ns}redirect-url" value="${redirectUrl}" />

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="fieldset.tab.info" /></legend>

        <div class="form-group">
            <input class="btn btn-block btn-primary" type="submit" value="<@liferay_ui['message'] key='button.save' />" />
        </div>

        <div class="form-group">
            <label for="${ns}tab-order"><@liferay_ui["message"] key="label.tab.order" /></label>
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
            <div class="form-group">
                <label for="${ns}title-${availableLocale}"><@liferay_ui["message"] key="fieldset.tag.info.title" /> (${availableLocale.getDisplayName(locale)})</label>
                <input class="form-control" type="text" id="${ns}title-${availableLocale}" name="${ns}title-${availableLocale}" value="${titleMap[availableLocale]!''}" />
            </div>
        </#list>
        <div class="form-group">
            <label for="${ns}page-size"><@liferay_ui["message"] key="label.page.size" /></label>
            <input class="form-control" type="text" id="${ns}page-size" name="${ns}page-size" value="${tabPageSize}" />
        </div>
        <div class="form-group">
            <label for="${ns}full-page-size"><@liferay_ui["message"] key="label.page.size.full" /></label>
            <input class="form-control" type="text" id="${ns}full-page-size" name="${ns}full-page-size" value="${tabFullPageSize}" />
        </div>
        <div class="form-group">
            <label for="${ns}load-more-page-size"><@liferay_ui["message"] key="label.page.size.load.more" /></label>
            <input class="form-control" type="text" id="${ns}load-more-page-size" name="${ns}load-more-page-size" value="${tabLoadMorePageSize}" />
        </div>
    </fieldset>

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="fieldset.tab.sorting" /></legend>

        <div class="form-group">
            <label for="${ns}sort-by"><@liferay_ui["message"] key="label.tab.sort.by" /></label>
            <select class="form-control" id="${ns}sort-by" name="${ns}sort-by">
                <option value="" ${sortBy?has_content?then('', 'selected="selected"')}><@liferay_ui["message"] key="label.tab.sort.by.none" /></option>
                <#list sortByAvailableFields as field>
                    <option value="${field}" ${(field == sortBy)?then('selected="selected"', '')}><@liferay_ui["message"] key="label.tab.sort.by.${field}" /></option>
                </#list>
            </select>
        </div>

        <div class="form-group">
            <label for="${ns}sort-reverse"><@liferay_ui["message"] key="label.tab.sort.reverse" /></label>
            <input type="checkbox" id="${ns}sort-reverse" name="${ns}sort-reverse" ${sortReverse?then('checked="checked"', '')} />
        </div>
    </fieldset>

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="fieldset.facets" /></legend>
        <p><@liferay_ui["message"] key="fieldset.facets.warning" /></p>
        <div class="form-group">
            <table class="table table-bordered">
                <summary><@liferay_ui["message"] key="table.facet.conf" /></summary>
                <thead>
                    <tr><th><@liferay_ui["message"] key="table.facets.head.facet" /></th><th><@liferay_ui["message"] key="table.facets.head.action" /></th></tr>
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
                                <a class="btn btn-default" href="${searchFacetUrls[facetClassName]}"><@liferay_ui["message"] key="action.configure" /></a>
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
        <input class="btn btn-block btn-primary" type="submit" value="<@liferay_ui['message'] key='button.save' />" />
    </div>

</form>
