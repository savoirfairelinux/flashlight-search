<#include "init.ftl" />

<@liferay_ui["success"] key="configuration.saved" message="Configuration saved" />

<form action="${saveGlobalUrl}" method="POST" name="${ns}global-form">

    <input type="hidden" name="${ns}redirect-url" value="${editGlobalUrl}" />

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="Appearance" /></legend>

        <div class="form-group input-select-wrapper">
            <label class="control-label" for="${ns}adt-uuid"><@liferay_ui["message"] key="Application display template" /></label>
            <select class="form-control" id="${ns}adt-uuid" name="${ns}adt-uuid">
                <optgroup label="<@liferay_ui['message'] key='Default' />">
                    <option value=""><@liferay_ui["message"] key="No ADT" /></option>
                </optgroup>
                <#list applicationDisplayTemplates?keys as key>
                    <optgroup label="${key}">
                        <#assign templates = applicationDisplayTemplates[key] />
                        <#list templates as template>
                            <#if template.uuid == adtUuid>
                                <option value="${template.uuid}" selected="selected">${template.getName(locale)}</option>
                            <#else>
                                <option value="${template.uuid}">${template.getName(locale)}</option>
                            </#if>
                        </#list>
                    </optgroup>
                </#list>
            </select>
        </div>
    </fieldset>

    <fieldset class="fieldset">
        <#if tabs?has_content>
            <legend><@liferay_ui["message"] key="Search tabs" /></legend>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th><@liferay_ui["message"] key="Order" /></th>
                        <th><@liferay_ui["message"] key="Title" /></th>
                        <th><@liferay_ui["message"] key="Structures" /></th>
                        <th><@liferay_ui["message"] key="Actions" /></th>
                    </tr>
                </thead>
                <tbody>
                <#list tabs?keys as tabId>
                    <#assign tab = tabs[tabId] />
                    <#assign contentTemplates = tab.getContentTemplates() />
                    <#assign editTabUrl = editTabUrls[tabId] />
                    <#assign deleteTabUrl = deleteTabUrls[tabId] />
                    <tr>
                        <td><a href="${editTabUrl}">${tab.order}</a></td>
                        <td><a href="${editTabUrl}">${tab.getTitle(locale)}</a></td>
                        <td>
                            <ul>
                                <#list availableStructures?keys as className>
                                    <#assign clStructures = availableStructures[className] />
                                    <#list clStructures as structure>
                                        <#if structure.templates?has_content>
                                            <#list structure.templates as template>
                                                <#if contentTemplates[structure.uuid]?? && contentTemplates[structure.uuid] == template.uuid>
                                                    <li><a href="${editTabUrl}">${structure.getName(locale)} => ${template.getName(locale)}</a></li>
                                                </#if>
                                            </#list>
                                        </#if>
                                    </#list>
                                </#list>
                            </ul>
                        </td>
                        <td>
                            <ul>
                                <li><a href="${editTabUrl}"><@liferay_ui["message"] key="Edit" /></a></li>
                                <li><a href="${deleteTabUrl}"><@liferay_ui["message"] key="Delete" /></a></li>
                            </ul>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </#if>
        <p><a href="${createTabUrl}"><@liferay_ui["message"] key="Create a new tab" /></a></p>
    </fieldset>

    <div class="form-group">
        <input class="btn btn-default" type="submit" value="<@liferay_ui['message'] key='Submit' />" />
    </div>

</form>
