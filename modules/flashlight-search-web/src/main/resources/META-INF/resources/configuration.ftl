<#include "init.ftl">

<@liferay_portlet["renderURL"] portletMode="edit" var="configurationRenderURL" />
<@liferay_portlet["actionURL"] name="configurePortlet" var="configurationURL" />

<@liferay_aui["form"] action="${configurationURL}" method="post" name="fm">
    <@liferay_aui["fieldset"] label="ADT">
        <div class="display-template">
            <@liferay_ddm["template-selector"]
                className="com.liferay.portal.kernel.search.Document"
                displayStyle=request.getPreferences().getValue("displayStyle","")
                displayStyleGroupId=request.getPreferences().getValue("displayStyleGroupId","0")?number
                refreshURL="${configurationRenderURL}"
                showEmptyOption=true
            />
        </div>
    </@>

    <@liferay_aui["fieldset"] label="Facets" >
        <@liferay_aui["select"] name="selected_facets" multiple=true label="">
            <#list searchFacets as facet>
                <@liferay_aui["option"] value="${facet.className}" label="${facet.title}" selected=enabled_facets?seq_contains(facet.className) />
            </#list>
        </@>
    </@>

    <@liferay_aui["fieldset"] label="Asset Entries">
        <#assign assets= request.getPreferences().getValues("selectedAssets",[]) />
        <@liferay_aui["select"] name="selected_assets_entries" multiple=true label="">
            <#list searchAssetEntries?keys as assetEntry>
                <@liferay_aui["option"] value="${assetEntry}" label="${searchAssetEntries[assetEntry]}" selected=assets?seq_contains(assetEntry) />
            </#list>
        </@>
    </@>

    <@liferay_aui["fieldset"] label="Structures">
        <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
            <#list structures as structure>
                <#assign selectdTemplate=request.getPreferences().getValue("ddm-"+structure.structureKey,"")>
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
