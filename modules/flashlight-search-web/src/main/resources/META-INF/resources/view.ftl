<#include "init.ftl">

<@liferay_portlet["renderURL"] varImpl="searchURL"/>

<@liferay_aui["form"]  action="${searchURL}" class="form-inline" name="fm" method="get">
    <@liferay_portlet_ext["renderURLParams"] varImpl="searchURL"/>
    <@liferay_aui["input"] name="mvcPath" type="hidden" value="/search_result.ftl" />
    <@liferay_aui["fieldset"]>
        <@liferay_aui["input"] class="search-input" label="" name="keywords" placeholder="search"  type="text" size="30" inlineField=true >
            <@liferay_aui["validator"] name="required" errorMessage="flashlight-validation-message"/>
        </@>
        <@liferay_aui["field-wrapper"] inlineField=true >
            <@liferay_aui["button"] type="submit" value="flashlight-search-button" class="icon-monospaced " icon="icon-search"></@>
        </@>
    </@>
</@>
