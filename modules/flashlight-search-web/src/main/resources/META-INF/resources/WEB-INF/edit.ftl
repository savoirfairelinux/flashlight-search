<#include "init.ftl">

<@liferay_ui["success"] key="configuration.saved" message="Configuration saved" />

<form action="${configureURL}" method="POST" name="${ns}preferences-form">

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
        <legend><@liferay_ui["message"] key="General options" /></legend>

        <div class="form-group input-select-wrapper">
            <label class="control-label" for="${ns}selected-facets"><@liferay_ui["message"] key="Facets" /></label>
            <select class="form-control" id="${ns}selected-facets"name="${ns}selected-facets" multiple="multiple">
                <#list searchFacets as facet>
                    <#if selectedFacets?seq_contains(facet.className)>
                        <option value="${facet.className}" selected="selected">${facet.title}</option>
                    <#else>
                        <option value="${facet.className}">${facet.title}</option>
                    </#if>
                </#list>
            </select>
        </div>

        <div class="form-group input-select-wrapper">
            <label class="control-label" for="${ns}selected-asset-types"><@liferay_ui["message"] key="Asset types" /></label>
            <select class="form-control" id="${ns}selected-asset-types" name="${ns}selected-asset-types" multiple="multiple">
                <#list supportedAssetTypes as assetType>
                    <#if selectedAssetTypes?seq_contains(assetType.name)>
                        <option value="${assetType.name}" selected="selected">${assetType.name}</option>
                    <#else>
                        <option value="${assetType.name}" />${assetType.name}</option>
                    </#if>
                </#list>
            </select>
        </div>
    </fieldset>

    <fieldset class="fieldet">
        <legend><@liferay_ui["message"] key="Structures" /></legend>
        <#list structures?keys as className>
            <#assign clStructures = structures[className] />
            <div class="form-group">
                <p><strong>${className}</strong></p>
                <#list clStructures as structure>
                    <#if structure.templates?has_content>
                        <div class="form-group input-select-wrapper">
                            <label class="control-label" for="${ns}ddm-${structure.uuid}">${structure.getName(locale)}</label>
                            <select class="form-control" id="${ns}ddm-${structure.uuid}" name="${ns}ddm-${structure.uuid}">
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
