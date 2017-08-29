<fieldset class="fieldet">
    <legend><@liferay_ui["message"] key="fieldset.journal.article" /></legend>

    <p><@liferay_ui["message"] key="fieldset.journal.article.description" /></p>

    <#list availableJournalArticleStructures?keys as className>
        <#assign clStructures = availableJournalArticleStructures[className] />
        <div class="form-group">
            <#list clStructures as structure>
                <#if structure.templates?has_content>
                    <div class="form-group">
                        <label class="control-label" for="${ns}journal-article-template-${structure.uuid}">${structure.getName(locale)}</label>
                        <select class="form-control" id="${ns}journal-article-template-${structure.uuid}" name="${ns}journal-article-template-${structure.uuid}">
                            <option value=""><@liferay_ui["message"] key="option.disabled" /></option>
                            <#list structure.templates as template>
                                <#if journalArticleTemplates[structure.uuid]?? && journalArticleTemplates[structure.uuid] == template.uuid>
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

<fieldset class="fieldset">
    <legend><@liferay_ui["message"] key="fieldset.journal.view" /></legend>

    <p><@liferay_ui["message"] key="fieldset.journal.view.description" /></p>

    <div class="form-group">
        <label class="control-label" for="${ns}journal-article-view-template"><@liferay_ui["message"] key="label.journal.view.adt" /></label>
        <select class="form-control" id="${ns}journal-article-view-template" name="${ns}journal-article-view-template">
            <option value=""><@liferay_ui["message"] key="adt.none" /></option>
            <#list availableJournalArticleViewTemplates?values as templates>
                <#list templates as template>
                    <#if journalArticleViewTemplate == template.uuid>
                        <option value="${template.uuid}" selected="selected">${template.getName(locale)}</option>
                    <#else>
                        <option value="${template.uuid}">${template.getName(locale)}</option>
                    </#if>
                </#list>
            </#list>
        </select>
    </div>
</fieldset>
