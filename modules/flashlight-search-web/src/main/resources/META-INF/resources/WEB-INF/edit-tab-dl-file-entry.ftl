<#--
Copyright 2017 Savoir-faire Linux
This file is part of Flashlight Search.

Flashlight Search is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Flashlight Search is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Flashlight Search.  If not, see <http://www.gnu.org/licenses/>.
-->
<fieldset class="fieldet">
    <legend><@liferay_ui["message"] key="fieldset.dlfileentry" /></legend>

    <p><@liferay_ui["message"] key="fieldset.dlfileentry.description" /></p>

    <#list availableDlFileEntryTypeTemplates?keys as dlFileEntryType>
        <#assign templates = availableDlFileEntryTypeTemplatesUuidIndex[dlFileEntryType.uuid] />
        <#if templates?has_content>
            <div class="form-group">
                <label for="${ns}dl-file-entry-type-template-${dlFileEntryType.uuid}">${dlFileEntryType.getName(locale)}</label>
                <select class="form-control" id="${ns}dl-file-entry-type-template-${dlFileEntryType.uuid}" name="${ns}dl-file-entry-type-template-${dlFileEntryType.uuid}">
                    <option value=""><@liferay_ui["message"] key="Disabled" /></option>
                    <#list templates as template>
                        <#if dlFileEntryTypeTemplates[dlFileEntryType.uuid]?? && dlFileEntryTypeTemplates[dlFileEntryType.uuid] == template.uuid>
                            <option value="${template.uuid}" selected="selected">${template.getName(locale)}</option>
                        <#else>
                            <option value="${template.uuid}">${template.getName(locale)}</option>
                        </#if>
                    </#list>
                </select>
            </div>
        </#if>
    </#list>
</fieldset>
