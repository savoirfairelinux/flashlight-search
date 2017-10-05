<#include "init.ftl">
<#assign hasLoadMore = false />

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
                       placeholder="<@liferay_ui['message'] key='action.search' />"
                       value="${keywords}">
            </div>
            <div class="lfr-ddm-field-group lfr-ddm-field-group-inline field-wrapper">
                <button class="btn btn-default"
                        type="submit"
                        name="${ns}submit">
                    <i class="icon-search"></i>
                    <span class="lfr-btn-label"><@liferay_ui['message'] key='action.search' /></span>
                </button>
            </div>
        </div>
    </fieldset>

    <#if resultsContainer.hasSearchResults()>
        <#assign searchPages = resultsContainer.searchPages />

        <div class="nav-tabs-scroll">
            <div class="nav-tabs-scrollbar">
                <ul class="nav nav-tabs nav-tabs-default">
                    <#if searchPages?size gt 1>
                        <li role="presentation" class="${tabId?has_content?then('','active')}">
                            <a href="${keywordUrl}" role="tab"><@liferay_ui["message"] key="tab.all" /> (${resultsContainer.totalSearchResults})</a>
                        </li>
                    </#if>
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
                <#assign showFacets = (tabId?has_content && page.getSearchFacets()?size > 0) />

                <div class="${showFacets?then("row","")}">
                    <#if showFacets>
                        <div class="col-md-3">
                            <#list page.searchFacets as searchFacet>
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <div class="panel-title">
                                            <strong><@liferay_ui["message"] key="facet.${searchFacet.label}" /></strong>
                                        </div>
                                    </div>
                                    <#if (searchFacet.terms?has_content && searchFacet.terms?size > 0)>
                                        <div class="panel-body">
                                            <#assign noTermsApplied = true />
                                            <#list searchFacet.terms as term>
                                                <#if term.applied >
                                                    <#assign noTermsApplied = false />
                                                </#if>
                                            </#list>
                                            <ul class="list-unstyled">
                                                <li>
                                                    <a class="${noTermsApplied?then('text-primary', 'text-default')}"
                                                       href="${tabUrls[tab.id]}&${ns}${searchFacet.fieldName}="
                                                       title="<@liferay_ui["message"] key="tab.all" />">
                                                        <@liferay_ui["message"] key="tab.all" />
                                                    </a>
                                                </li>
                                                <#list searchFacet.terms as term>
                                                    <#if (term.frequency > 0)>
                                                        <#assign prettyPrintTerm = facetTerm.apply(searchFacet, term.getTerm()) />
                                                        <#assign termUrl = tabUrls[tab.id] />
                                                        <#list term.urlTerms?keys as termParam>
                                                            <#assign termUrl = termUrl + '&' + ns + termParam + '=' + term.urlTerms[termParam] />
                                                        </#list>
                                                        <li>
                                                            <a class="${(term.applied)?then('text-primary', 'text-default')}"
                                                               href="${termUrl}"
                                                               title="${prettyPrintTerm}">
                                                                ${prettyPrintTerm} (${term.frequency})
                                                            </a>
                                                        </li>
                                                    </#if>
                                                </#list>
                                            </ul>
                                        </div>
                                    </#if>
                                </div>
                            </#list>
                        </div>
                    </#if>

                    <div class="${showFacets?then("col-md-9","")}">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <div class="panel-title">
                                    <strong>${tab.getTitle(locale)}</strong> <span>(${page.totalSearchResults})</span>
                                </div>
                            </div>
                            <div class="panel-body">
                                <ul id="${ns}results-container" class="display-style-descriptive tabular-list-group">
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
                                <#if loadMoreUrls[tab.id]??>
                                    <#assign hasLoadMore = true />
                                    <button class="btn btn-block btn-default" type="button" id="${ns}load-more" data-load-more-url="${loadMoreUrls[tab.id]}">
                                        <@liferay_ui["icon"]
                                            icon="plus"
                                            markupView="lexicon"
                                            message="Load more"
                                        />
                                        <@liferay_ui["message"] key="action.load.more" />
                                    </button>
                                </#if>
                            </div>
                        </div>
                    </div>
                </div>
            </#if>
        </#list>
    </#if>
</form>

<#if hasLoadMore>

    <script id="${ns}load-more-template" type="text/template">
        <li class="list-group-item">
            <div class="list-group-item-content">
                <h5><strong><a href="{{ url }}" title="{{ title }}">{{ title }}</a></strong></h5>
                {{ html }}
            </div>
        </li>
    </script>

    <script type="text/javascript">

        /**
         * This method initiates the "load more" functionality
         */
        function com_savoirfairelinux_flashlight_portlet_initLoadMore() {
            // Instanciate portlet client-side class with server-provided portlet namespace
            var portlet = new com_savoirfairelinux_flashlight_portlet.FlashlightSearchPortlet('${ns}');

            // This is the template used to append new search results
            var resultElementTemplate = portlet.getElementById('load-more-template').innerHTML;
            var urlRegex = /\{\{\ *url *}\}/g;
            var titleRegex = /\{\{ *title *\}\}/g;
            var htmlRegex = /\{\{ *html *\}\}/g;

            // Bind the "load more" event to the "load more" link, using the "click" event and the "href" attribute
            var loadMoreElement = portlet.getElementById('load-more');
            portlet.bindLoadMore(loadMoreElement, 'click', 'data-load-more-url',

                /**
                 * Load more progressing
                 */
                function(progressEvent, element) {},

                /**
                 * Load more succeedeed
                 */
                function(successEvent, element, jsonResponse) {
                    // Create an HTML element containing the search result and append it to the search results list
                    var results = jsonResponse.results;
                    var loadMoreUrl = jsonResponse.loadMoreUrl;
                    var resultsSize = results.length;
                    var resultsContainer = portlet.getElementById('results-container');
                    var resultsToInsert = '';

                    for(var i = 0; i < resultsSize; i++) {
                        var resultElementContent = resultElementTemplate.replace(urlRegex, results[i].url);
                        resultElementContent = resultElementContent.replace(titleRegex, results[i].title);
                        resultElementContent = resultElementContent.replace(htmlRegex, results[i].html);
                        resultsToInsert += resultElementContent;
                    }

                    resultsContainer.insertAdjacentHTML('beforeend', resultsToInsert);

                    // If the XHR returned no "load more" URLs, we remove the link as it is no longer needed
                    if( !loadMoreUrl ) {
                        element.parentNode.removeChild(element);
                    }
                },

                /**
                 * Load more failed
                 */
                function(errorEvent, element) {},

                /**
                 * Load more aborted
                 */
                function(abortedEvent, element) {}

            );

        }

        if(typeof Liferay !== 'undefined') {
            // Liferay Javascript available, use Liferay's events
            Liferay.Portlet.ready(function(portletId, node) {
                var choppedNs = '${ns}';
                choppedNs = choppedNs.substring(1, choppedNs.length - 1);
                if(portletId === choppedNs) {
                    com_savoirfairelinux_flashlight_portlet_initLoadMore();
                }
            });
        } else {
            // Vanilla theme not using Liferay's Javascript
            document.addEventListener('DOMContentLoaded', com_savoirfairelinux_flashlight_portlet_initLoadMore);
        }

    </script>
</#if>
