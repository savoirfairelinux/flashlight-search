<#include "init.ftl">

<#assign params = Request["com.liferay.portal.kernel.servlet.PortletServletRequest"] />
<#assign results=Request.searchResults />


<@liferay_ddm["template-renderer"]
    className="com.liferay.portal.kernel.search.Document"
    displayStyle=Request.renderRequest.getPreferences().getValue("displayStyle","")
    displayStyleGroupId=Request.renderRequest.getPreferences().getValue("displayStyleGroupId","0")?number
    entries=results
>

<@liferay_portlet["renderURL"] var="viewURL">
    <@liferay_portlet["param"] name="mvcPath" value="/view.ftl" />
</@>
<@liferay_portlet["renderURL"] varImpl="searchURL"/>


<@liferay_aui["form"] action="${searchURL}" name="search_form" method="get">
    <@liferay_portlet_ext["renderURLParams"] varImpl="searchURL"/>

    <@liferay_aui["input"] name="ddmStructureKey"  type="hidden" value=params.getParameter("ddmStructureKey")!"" />
    <@liferay_aui["input"] name="fileEntryTypeId" type="hidden"  value=params.getParameter("fileEntryTypeId")!""/>
    <@liferay_aui["input"] name="assetCategoryIds" type="hidden"  value=params.getParameter("assetCategoryIds")!""/>
    <@liferay_aui["input"] name="entryClassName" type="hidden"  value=params.getParameter("entryClassName")!""/>

    <@liferay_aui["input"] name="createDate" type="hidden"   value=params.getParameter("createDate")!"" />
    <@liferay_aui["input"] name="mvcPath" type="hidden" value="/search_result.ftl" />
    <@liferay_aui["fieldset"]>
        <@liferay_aui["input"] class="search-input" label="" name="keywords" placeholder="search"  type="text" size="30" inlineField=true  >
            <@liferay_aui["validator"] name="required" errorMessage="flashlight-validation-message"/>
        </@>
        <@liferay_aui["field-wrapper"]  inlineField=true >
            <@liferay_aui["button"] type="submit"   value="flashlight-search-button" class="icon-monospaced " icon="icon-search"></@>
        </@>
    </@>

    <nav class="navbar navbar-default">
        <div class="container-fluid">
            <div class="collapse navbar-collapse">
                <ul class="nav navbar-nav">
                    <@liferay_portlet["renderURL"] var="facetURL">
                        <@liferay_portlet["param"] name="mvcPath" value="/search_result.ftl" />
                        <@liferay_portlet["param"] name="keywords" value=Request.keywords />
                    </@>
                    <li><a href="${facetURL}" ><@liferay_ui["message"] key="flashlight-all-results" /></a></li>
                    <#list Request.searchFacets as facet>
                        <#list facet.facet.facetCollector.termCollectors as term>
                            <#if (Request.groupedDocuments[term.term])?? && Request.enabled_facets?seq_contains(facet.className) >
                                <li><@liferay_aui["a"] href="#" onClick="facetFilter(['${facet.fieldName}' , '${term.term}'])" >${Request.facets[term.term]!term.term} <span class="badge">${term.frequency}</span></@></li>
                            </#if>
                        </#list>
                    </#list>
                </ul>
            </div>
        </div>
    </nav>
    <#assign current_year = .now?string.yyyy?number  >
    <#assign years = [current_year..2000][0] />

    <#assign ddmStructureKey=params.getParameter("ddmStructureKey")!"" />
    <#assign fileEntryTypeId = params.getParameter("fileEntryTypeId")!"" />
    <#assign entryClassName = params.getParameter("entryClassName")!"" />
    <#assign isShowing = ddmStructureKey!="" || fileEntryTypeId!="" || entryClassName!=""/>
    <#if isShowing >
        <div class="row">
            <div class="col-md-9"></div>
            <#if Request.enabled_facets?seq_contains("com.savoirfairelinux.facet.CreatedSearchFacet") >
                <div class="col-md-1">
                    <@liferay_aui["select"] name="create" onChange="filterByYear(this)" label="flashlight-years">
                        <@liferay_aui["option"] value="any" label="flashlight-any" />
                        <#list years as year >
                            <@liferay_aui["option"] value=year label=year />
                        </#list>
                    </@>
                </div>
            </#if>
            <#if Request.enabled_facets?seq_contains("com.liferay.portal.search.web.internal.facet.AssetCategoriesSearchFacet") >
                <div class="col-md-2">
                    <@liferay_aui["select"] name="vocabulary" id="vocabulary" label="flashlight-vocabulary" onChange="vocabularyFilter(this)">
                        <@liferay_aui["option"] label="flashlight-any" value=""  />
                        <#list Request.categories as category>
                            <@liferay_aui["option"] label=category.name value=category.categoryId />
                        </#list>
                    </@>
                </div>
            </#if>
        </div>
    </#if>
    <@liferay_aui["script"]>
        function filterByYear(select){
            if(select.value != "any"){
                var range = "["+select.value+"0101000000 TO "+select.value+"1231235959 ]";
                $("#<@liferay_portlet["namespace"] />createDate").val(range);
            }
            else{
                $("#<@liferay_portlet["namespace"] />createDate").val("");
            }
            $("#<@liferay_portlet["namespace"] />search_form").submit();
        }

        function facetFilter(facet){
            $("#<@liferay_portlet["namespace"] />"+facet[0]).val(facet[1]);
            $("#<@liferay_portlet["namespace"] />search_form").submit();
        }

        function vocabularyFilter(select){
            $("#<@liferay_portlet["namespace"] />assetCategoryIds").val(select.value);
            $("#<@liferay_portlet["namespace"] />search_form").submit();
        }
    </@>
</@>

<div class="container">
    <#if results?has_content >
        <#list results as group>
            <#assign key = group.key />
            <div class="panel panel-default">
                <div class="panel-heading">
                    ${Request.facets[key]!key} <span class="badge">${group.documents?size}</span>
                </div>
                <div class="panel-body">
                    <#assign groupdocuments = group.documents />
                    <#if ddmStructureKey=="" && fileEntryTypeId=="">
                        <#assign groupdocuments = group.documents[0..*3] />
                    </#if>
                    <#list groupdocuments as document>
                        <#if document?index%3==0 >
                            <div class="row">
                        </#if>
                        <div class="col-md-4">
                            <#assign entryUrl = Request.flashlightUtil.getAssetViewURL(Request.renderRequest, Request.renderResponse, document)>
                            <#if document.entryClassName == "com.liferay.journal.model.JournalArticle">
                                ${document.get("journalContent")?replace("{entryUrl}", entryUrl)}
                            <#else>
                                <h2><a href="${entryUrl}">${document.get("title")}</a></h2>
                            </#if>
                        </div>

                        <#if document?index%3==2 >
                            </div>
                        </#if>
                    </#list>
                    <#if groupdocuments?size%3 !=0>
                        </div>
                    </#if>
                    <#if !isShowing>
                        <div class="row">
                            <div class="col-md-12 center-block">
                                <#assign param_name= groupdocuments[0].get("type") />
                                <@liferay_aui["button"] type="button" onClick="facetFilter(['${param_name}' , '${key}'])" cssClass="btn btn-default center-block" value="flashlight-show-more"/>
                            </div>
                        </div>
                    </#if>
                </div>
            </div>
        </#list>
    <#else>
        <div class="col-md-12 center-block text-center">
            <@liferay_ui["message"] key="flashlight-empty-results" />
        </div>
    </#if>
    <hr/>
    <div>
        <a href="${viewURL}" class="btn"><@liferay_ui["message"] key="flashlight-return-button" /></a>
    </div>
</div>
</@>
