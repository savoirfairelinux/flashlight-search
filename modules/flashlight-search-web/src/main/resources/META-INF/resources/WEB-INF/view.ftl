<#include "init.ftl">

<@liferay_portlet["renderURL"] varImpl="renderURL" />
<form action="${renderURL}" name="${ns}search" method="GET">
    <#-- Needed for the form to work in GET method -->
    <@liferay_portlet_ext["renderURLParams"] varImpl="renderURL" />

    <fieldset>
        <input type="text" name="${ns}keywords" placeholder="<@liferay_ui['message'] key='search' />" value="${keywords}">
        <input type="submit" name="${ns}submit" value="<@liferay_ui['message'] key='search' />" />
    </fieldset>

    <#assign searchResults = resultsContainer.searchResults />
    <ul>
        <li><a href="${keywordUrl}"><@liferay_ui["message"] key="flashlight-all-results" /></a></li>
        <#list searchResults?keys as tab>
            <#if resultsContainer.hasSearchResults(tab)>
                <li><a href="#">${tab.getTitle(locale)}</a></li>
            </#if>
        </#list>
    </ul>
    <ul>
        <#list searchResults?keys as tab>
            <#if resultsContainer.hasSearchResults(tab)>
                <li>
                    <p><strong>${tab.getTitle(locale)}</strong></p>
                    <ul>
                        <#list resultsContainer.getSearchResults(tab) as result>
                            <li><a href="${result.viewUrl!'#'}" title="${result.title}">${result.rendering}</a></li>
                        </#list>
                    </ul>
                </li>
            </#if>
        </#list>
    </ul>
</form>
