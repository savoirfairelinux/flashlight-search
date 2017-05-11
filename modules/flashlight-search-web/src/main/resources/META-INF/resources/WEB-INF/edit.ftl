<#include "init.ftl" />

<@liferay_ui["success"] key="configuration.saved" message="Configuration saved" />

<form action="${saveGlobalUrl}" method="POST" name="${ns}global-form">

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="Appearance" /></legend>

        <div class="form-group input-select-wrapper">
            <label class="control-label" for="${ns}adt-uuid"><@liferay_ui["message"] key="Application display template" /></label>
            <select class="form-control" id="${ns}adt-uuid" name="${ns}adt-uuid">
                <optgroup label="<@liferay_ui['message'] key='Default' />">
                    <option value=""><@liferay_ui["message"] key="No ADT" /></option>
                </optgroup>
                <#list applicationDisplayTemplates?keys as key>
                    <optgroup label="${key.getName(locale)}">
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
            <#list tabs?keys as tabId>
                <ul>
                    <li><a href="${editTabUrls[tabId]}">${tabs[tabId].getTitle(locale)}</a></li>
                </ul>
            </#list>
        </#if>
        <p><a href="${createTabUrl}"><@liferay_ui["message"] key="Create a new tab" /></a></p>
    </fieldset>

    <div class="form-group">
        <input class="btn btn-default" type="submit" value="<@liferay_ui['message'] key='Submit' />" />
    </div>

</form>
