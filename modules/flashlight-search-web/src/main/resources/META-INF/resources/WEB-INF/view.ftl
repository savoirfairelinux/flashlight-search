<#include "init.ftl">

<@liferay_portlet["renderURL"] varImpl="renderURL" />
<form action="${renderURL}" name="${ns}search" method="GET">
    <#-- Needed for the form to work in GET method -->
    <@liferay_portlet_ext["renderURLParams"] varImpl="renderURL" />

    <fieldset>
        <input type="text" name="${ns}keywords" placeholder="<@liferay_ui['message'] key='search' />" value="${keywords}">
        <input type="submit" name="${ns}submit" value="<@liferay_ui['message'] key='search' />" />
    </fieldset>

    <#if resultsContainer.hasSearchResults()>
        <#assign searchPages = resultsContainer.searchPages />
        <ul>
            <li><a href="${keywordUrl}"><@liferay_ui["message"] key="flashlight-all-results" /></a></li>
            <#list searchPages?keys as tab>
                <#if resultsContainer.hasSearchResults(tab)>
                    <li><a href="#">${tab.getTitle(locale)} (${resultsContainer.getSearchPage(tab).totalSearchResults})</a></li>
                </#if>
            </#list>
        </ul>
        <ul>
            <#list searchPages?keys as tab>
                <#if resultsContainer.hasSearchResults(tab)>
                    <#assign page = resultsContainer.getSearchPage(tab) />
                    <li>
                        <p><strong>${tab.getTitle(locale)}</strong><span>(${page.totalSearchResults})</span></p>
                        <ul>
                            <#list page.searchResults as result>
                                <li><a href="${result.viewUrl!'#'}" title="${result.title}">${result.rendering}</a></li>
                            </#list>
                        </ul>
                    </li>
                </#if>
            </#list>
        </ul>
    </#if>
</form>
