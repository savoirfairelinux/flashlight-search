<#include "init.ftl">

<@liferay_portlet["renderURL"] varImpl="renderURL" />
<form action="${renderURL}" name="${ns}search" method="GET">
    <#-- Needed for the form to work in GET method -->
    <@liferay_portlet_ext["renderURLParams"] varImpl="renderURL" />

    <#-- Search parameters -->
    <input type="hidden" name="${ns}ddmStructureKey" value="${request.getParameter('ddmStructureKey')!''}" />
    <input type="hidden" name="${ns}fileEntryTypeId" value="${request.getParameter('fileEntryTypeId')!''}" />
    <input type="hidden" name="${ns}assetCategoryIds" value="${request.getParameter('assetCategoryIds')!''}" />
    <input type="hidden" name="${ns}entryClassName" value="${request.getParameter('entryClassName')!''}" />
    <input type="hidden" name="${ns}createDate" value="${request.getParameter('createDate')!''}" />

    <fieldset>
        <input type="text" name="${ns}keywords" placeholder="<@liferay_ui['message'] key='search' />">
        <input type="submit" name="${ns}submit" value="<@liferay_ui['message'] key='search' />" />
    </fieldset>

    <#if groupedDocuments?has_content>
        <ul>
            <li><a href="${keywordUrl}"><@liferay_ui["message"] key="flashlight-all-results" /></a></li>
        </ul>
        <ul>
            <#list groupedDocuments?keys as key>
                <#assign documents = groupedDocuments[key] />
                <li>
                    <div>
                        <strong>${key}</strong>
                        <span>${documents?size}</span>
                    </div>
                    <ul>
                        <#list documents as document>
                        <li>
                            <#assign entryUrl = flashlightUtil.getAssetViewURL(request, response, document)>
                            <#if document.entryClassName == "com.liferay.journal.model.JournalArticle">
                                ${document.get("journalContent")?replace("{entryUrl}", entryUrl)}
                            <#else>
                                <a href="${entryUrl}">${document.get("title")}</a>
                            </#if>
                        </li>
                        </#list>
                    </ul>
                </li>
            </#list>
        </ul>
    <#elseif (keywords?? && keywords != '')>
        <div><@liferay_ui["message"] key="flashlight-empty-results" /></div>
    </#if>
</form>
