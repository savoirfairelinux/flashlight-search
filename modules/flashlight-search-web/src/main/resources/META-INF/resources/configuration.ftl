<#include "init.ftl">

<@liferay_portlet["renderURL"] portletConfiguration=true var="configurationRenderURL" />

<@liferay_portlet["actionURL"] name="configurationURL" var="configurationURL" />
<@liferay_aui["form"] action="${configurationURL}" method="post" name="fm">
    <@liferay_aui["fieldset"] label="ADT">
        <div class="display-template">
            <@liferay_ddm["template-selector"]
                className="com.liferay.portal.kernel.search.Document"
                displayStyle=Request.renderRequest.getPreferences().getValue("displayStyle","")
                displayStyleGroupId=Request.renderRequest.getPreferences().getValue("displayStyleGroupId","0")?number
                refreshURL="${configurationRenderURL}"
                showEmptyOption=true
            />
        </div>
    </@>

    <@liferay_aui["fieldset"] label="Facets" >
        <@liferay_aui["select"] name="selected_facets" multiple=true label="">
            <#list Request.searchFacets as facet>
                <@liferay_aui["option"] value="${facet.className}" label="${facet.title}" selected=Request.enabled_facets?seq_contains(facet.className) />
            </#list>
        </@>
    </@>

    <@liferay_aui["fieldset"] label="Asset Entries">
        <#assign assets= Request.renderRequest.getPreferences().getValues("selectedAssets",[]) />
        <@liferay_aui["select"] name="selected_assets_entries" multiple=true label="">
            <#list Request.searchAssetEntries?keys as assetEntry>
                <@liferay_aui["option"] value="${assetEntry}" label="${Request.searchAssetEntries[assetEntry]}" selected=assets?seq_contains(assetEntry) />
            </#list>
        </@>
    </@>

    <@liferay_aui["fieldset"] label="Structures">
        <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
            <#list Request.structures as structure>
                <#assign selectdTemplate=Request.renderRequest.getPreferences().getValue("ddm-"+structure.structureKey,"")>
                <div class="panel panel-default">
                    <div class="panel-body ">
                        <@liferay_aui["select"] label=structure.getName(locale) name="ddm-"+structure.structureKey >
                            <#list structure["templates"] as template>
                                <@liferay_aui["option"] label=template.getName(locale) value=template.templateKey  selected=(selectdTemplate==template.templateKey)/>
                            </#list>
                        </@>
                    </div>
                </div>
            </#list>
        </div>
    </@>

    <@liferay_aui["button-row"]>
        <@liferay_aui["button"] type="submit" />
    </@>
</@>
