<#include "init.ftl" />

<@liferay_ui["success"] key="configuration.saved" message="Configuration saved" />

<p><a href="${editGlobalUrl}"><@liferay_ui["message"] key="Back to global configuration" /></a></p>

<form action="${saveTabUrl}" method="POST" name="${ns}tab-form">
    <#if tabId?? && tabId != "">
        <input type="hidden" name="${ns}tab-id" value="${tabId}" />
        <input type="hidden" name="${ns}edit-mode" value="tab" />
    </#if>

    <input type="hidden" name="${ns}redirect-url" value="${redirectUrl}" />

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="Tab information" /></legend>
        <div class="form-group input-select-wrapper">
            <label for="${ns}tab-order"><@liferay_ui["message"] key="Tab order" /></label>
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
            <div class="form-group input-text-wrapper">
                <label for="${ns}title-${availableLocale}">${availableLocale.getDisplayName(locale)}</label>
                <input type="text" id="${ns}title-${availableLocale}" name="${ns}title-${availableLocale}" value="${titleMap[availableLocale]!''}" />
            </div>
        </#list>
    </fieldset>

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="Facet configuration" /></legend>

        <div class="form-group input-select-wrapper">
            <label class="control-label" for="${ns}asset-types"><@liferay_ui["message"] key="Asset types" /></label>
            <select class="form-control" id="${ns}asset-types" name="${ns}asset-types" multiple="multiple">
                <#list supportedAssetTypes as assetType>
                    <#if assetTypes?seq_contains(assetType)>
                        <option value="${assetType}" selected="selected">${assetType}</option>
                    <#else>
                        <option value="${assetType}" />${assetType}</option>
                    </#if>
                </#list>
            </select>
        </div>
    </fieldset>

    <fieldset class="fieldet">
        <legend><@liferay_ui["message"] key="Rendering" /></legend>
        <#list availableStructures?keys as className>
            <#assign clStructures = availableStructures[className] />
            <div class="form-group">
                <p><strong>${className}</strong></p>
                <#list clStructures as structure>
                    <#if structure.templates?has_content>
                        <div class="form-group input-select-wrapper">
                            <label class="control-label" for="${ns}ddm-${structure.uuid}">${structure.getName(locale)}</label>
                            <select class="form-control" id="${ns}ddm-${structure.uuid}" name="${ns}ddm-${structure.uuid}">
                                <option value=""><@liferay_ui["message"] key="Disabled" /></option>
                                <#list structure.templates as template>
                                    <#if contentTemplates[structure.uuid]?? && contentTemplates[structure.uuid] == template.uuid>
                                        <option value="${template.uuid}" selected="selected">${template.getName(locale)}</option>
                                    <#else>
                                        <option value="${template.uuid}">${template.getName(locale)}</option>
                                    </#if>
                                </#list>
                            </select>
                        </div>
                    </#if>
                </#list>
            </div>
        </#list>
    </fieldset>

    <div class="form-group">
        <input class="btn btn-default" type="submit" value="<@liferay_ui['message'] key='Submit' />" />
    </div>

</form>
