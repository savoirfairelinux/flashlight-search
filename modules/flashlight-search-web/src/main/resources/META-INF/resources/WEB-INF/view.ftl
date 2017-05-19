<#include "init.ftl">

<@liferay_portlet["renderURL"] varImpl="renderURL" />
<form action="${renderURL}" name="${ns}search" method="GET">
    <#-- Needed for the form to work in GET method -->
    <@liferay_portlet_ext["renderURLParams"] varImpl="renderURL" />

    <fieldset class="fieldset ">
        <div class="">
            <div class="form-group form-group-inline input-text-wrapper">
                <input class="field form-control"
                       type="text"
                       size="30"
                       name="${ns}keywords"
                       placeholder="<@liferay_ui['message'] key='search' />"
                       value="${keywords}">
            </div>
            <div class="lfr-ddm-field-group lfr-ddm-field-group-inline field-wrapper">
                <button class="btn btn-default"
                        type="submit"
                        name="${ns}submit">
                    <i class="icon-search"></i>
                    <span class="lfr-btn-label"><@liferay_ui['message'] key='search' /></span>
                </button>
            </div>
        </div>
    </fieldset>

    <#if resultsContainer.hasSearchResults()>
        <#assign searchPages = resultsContainer.searchPages />

        <div class="nav-tabs-scroll">
            <div class="nav-tabs-scrollbar">
                <ul class="nav nav-tabs nav-tabs-default">
                    <li role="presentation" class="${tabId?has_content?then("","active")}">
                        <a href="${keywordUrl}" role="tab"><@liferay_ui["message"] key="flashlight-all-results" /> (${resultsContainer.totalSearchResults})</a>
                    </li>
                    <#list searchPages?keys as tab>
                        <#if resultsContainer.hasSearchResults(tab)>
                            <li role="presentation" class="${(tabId?has_content && tabId == tab.id)?then("active","")}">
                                <a href="${tabUrls[tab.id]}" role="tab">${tab.getTitle(locale)} (${resultsContainer.getSearchPage(tab).totalSearchResults})</a>
                            </li>
                        </#if>
                    </#list>
                </ul>
            </div>
        </div>

        <#list searchPages?keys as tab>
            <#if resultsContainer.hasSearchResults(tab) && (! tabId?has_content || tabId == tab.id) >
                <#assign page = resultsContainer.getSearchPage(tab) />
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <div class="panel-title">
                            <strong>${tab.getTitle(locale)}</strong> <span>(${page.totalSearchResults})</span>
                        </div>
                    </div>
                    <div class="panel-body">
                        <ul class="display-style-descriptive tabular-list-group">
                            <#list page.searchResults as result>
                                <li class="list-group-item">
                                    <div class="list-group-item-content">
                                        <h5>
                                            <strong>
                                                <a href="${result.viewUrl!'#'}" title="${result.title}">${result.title}</a>
                                            </strong>
                                        </h5>
                                        ${result.rendering}
                                    </div>
                                </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </#if>
        </#list>
    </#if>
</form>
