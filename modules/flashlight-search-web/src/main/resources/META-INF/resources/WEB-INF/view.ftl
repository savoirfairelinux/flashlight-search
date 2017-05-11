<#include "init.ftl">

<@liferay_portlet["renderURL"] varImpl="renderURL" />
<form action="${renderURL}" name="${ns}search" method="GET">
    <#-- Needed for the form to work in GET method -->
    <@liferay_portlet_ext["renderURLParams"] varImpl="renderURL" />

    <fieldset>
        <input type="text" name="${ns}keywords" placeholder="<@liferay_ui['message'] key='search' />" value="${keywords}">
        <input type="submit" name="${ns}submit" value="<@liferay_ui['message'] key='search' />" />
    </fieldset>

    <#assign results = resultsContainer.searchResults />
    <#if results?has_content>
        <ul>
            <li><a href="${keywordUrl}"><@liferay_ui["message"] key="flashlight-all-results" /></a></li>
            <#list results?keys as structure>
                <#if resultsContainer.hasSearchResults(structure)>
                    <li>${structure.getName(locale)}</li>
                </#if>
            </#list>
        </ul>
        <ul>
            <#list results?keys as structure>
                <#if resultsContainer.hasSearchResults(structure)>
                    <li>
                        <p><strong>${structure.getName(locale)}</strong></p>
                        <ul>
                            <#list resultsContainer.getSearchResults(structure) as result>
                                <li><a href="${result.viewUrl!'#'}" title="${result.title}">${result.rendering}</a></li>
                            </#list>
                        </ul>
                    </li>
                </#if>
            </#list>
        </ul>
    </#if>
</form>
