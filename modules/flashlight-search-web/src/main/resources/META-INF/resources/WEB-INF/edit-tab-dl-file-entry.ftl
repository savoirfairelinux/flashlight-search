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
