<#include "init.ftl" />

<@liferay_ui["success"] key="configuration.saved" message="Configuration saved" />

<form action="${saveGlobalUrl}" method="POST" name="${ns}global-form">

    <input type="hidden" name="${ns}redirect-url" value="${editGlobalUrl}" />

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="fieldset.behavior" /></legend>
        <div class="form-group">
            <label for="${ns}do-search-on-startup"><@liferay_ui["message"] key="label.do.search.startup" /></label>
            <#assign startupChecked = "" />
            <#if doSearchOnStartup>
                <#assign startupChecked = " checked=\"checked\"" />
            </#if>
            <input type="checkbox" id="${ns}do-search-on-startup" name="${ns}do-search-on-startup"${startupChecked} />
        </div>
    </fieldset>

    <fieldset class="fieldset">
        <legend><@liferay_ui["message"] key="fieldset.appearance" /></legend>

        <div class="form-group">
            <label for="${ns}adt-uuid"><@liferay_ui["message"] key="label.adt" /></label>
            <select class="form-control" id="${ns}adt-uuid" name="${ns}adt-uuid">
                <optgroup label="<@liferay_ui['message'] key='adt.default' />">
                    <option value=""><@liferay_ui["message"] key="adt.none" /></option>
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
                        <th><@liferay_ui["message"] key="table.tabs.head.order" /></th>
                        <th><@liferay_ui["message"] key="table.tabs.head.title" /></th>
                        <th><@liferay_ui["message"] key="table.tabs.head.mapping" /></th>
                        <th><@liferay_ui["message"] key="table.tabs.head.actions" /></th>
                    </tr>
                </thead>
                <tbody>
                <#list tabs?keys as tabId>
                    <#assign tab = tabs[tabId] />
                    <#assign contentTemplates = tab.journalArticleTemplates />
                    <#assign dlFileEntryTypeTemplates = tab.getDLFileEntryTypeTemplates() />
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
                                                    <li><a href="${editTabUrl}">${structure.getName(locale)} <span class="icon-arrow-right"></span> ${template.getName(locale)}</a></li>
                                                </#if>
                                            </#list>
                                        </#if>
                                    </#list>
                                </#list>
                                <#list availableDlFileEntryTypeTemplates?keys as dlFileEntryType>
                                    <#assign templates = availableDlFileEntryTypeTemplatesUuidIndex[dlFileEntryType.uuid] />
                                    <#list templates as template>
                                        <#if dlFileEntryTypeTemplates[dlFileEntryType.uuid]?? && dlFileEntryTypeTemplates[dlFileEntryType.uuid] == template.uuid>
                                            <li><a href="${editTabUrl}">${dlFileEntryType.getName(locale)} <span class="icon-arrow-right"></span> ${template.getName(locale)}</a></li>
                                        </#if>
                                    </#list>
                                </#list>
                            </ul>
                        </td>
                        <td>
                            <div class="toolbar-group-content">
                                <a class="dropdown-toggle" data-toggle="dropdown" href="#"><span class="icon-cog"></span></a>
                                <ul class="dropdown-menu dropdown-menu-right">
                                    <li><a href="${editTabUrl}"><@liferay_ui["message"] key="action.edit" /></a></li>
                                    <li><a href="${deleteTabUrl}"><@liferay_ui["message"] key="action.delete" /></a></li>
                                </ul>
                            <div>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </#if>

        <div class="form-group btn-group dropdown">
            <button class="btn btn-default dropdown-toggle" data-toggle="dropdown" type="button"><@liferay_ui["message"] key="button.create.tab" /> <span class="caret"></span></button>

            <ul class="dropdown-menu" role="menu">
            <#list createTabUrls?keys as assetType>
                <li><a href="${createTabUrls[assetType]}"><@liferay_ui["message"] key="asset.type.${assetType}" /></a></li>
            </#list>
            </ul>
        </div>
    </fieldset>

    <div class="form-group">
        <input class="btn btn-block btn-primary" type="submit" value="<@liferay_ui['message'] key='button.save' />" />
    </div>

</form>
