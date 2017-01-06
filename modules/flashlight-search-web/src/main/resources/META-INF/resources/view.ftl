<#assign liferay_portlet = taglibLiferayHash["/META-INF/liferay-portlet.tld"] />
<#assign liferay_portlet_ext = taglibLiferayHash["/META-INF/liferay-portlet-ext.tld"] />
<#assign liferay_security = taglibLiferayHash["/META-INF/liferay-security.tld"] />
<#assign liferay_theme = taglibLiferayHash["/META-INF/liferay-theme.tld"] />
<#assign liferay_aui = taglibLiferayHash["/META-INF/liferay-aui.tld"] />

<#--  
<#if false>
<h1>Flashlight search</h1>
<@liferay_portlet["renderURL"] var="searchURL">
				<@liferay_portlet["param"] name="mvcPath" value="/search_result.ftl" />				
</@>
			
			<@liferay_aui["form"] action="${searchURL}" >
				<@liferay_aui["input"] name="keyword" label="" />
				
				<@liferay_aui["button"] type="submit" >
					<@liferay_ui["icon"] class="icon-monospaced" icon="search" markupView="lexicon"  />
				</@>
				
			</@>
</#if>
-->

<@liferay_portlet["renderURL"] varImpl="searchURL"/>

<@liferay_aui["form"]  action="${searchURL}" class="form-inline" name="fm" method="get">
	<@liferay_portlet_ext["renderURLParams"] varImpl="searchURL"/>
	<@liferay_aui["input"] name="mvcPath" type="hidden" value="/search_result.ftl" />
	<@liferay_aui["fieldset"]>
		<@liferay_aui["input"] class="search-input" label="" name="keywords" placeholder="search"  type="text" size="30" inlineField=true >
			<@liferay_aui["validator"] name="required" errorMessage="You must enter a search term"/>
		</@>
		<@liferay_aui["field-wrapper"] inlineField=true >
			<@liferay_aui["button"] type="submit" value="Search" class="icon-monospaced " icon="icon-search"></@>
			<#if false>
				<@liferay_ui["icon"] class="icon-monospaced" icon="search" markupView="lexicon" onclick='alert("test");' inlineField=true url="javascript:;" />
			</#if>
		</@>
	</@>
	
	<@liferay_aui["script"]>
		function search() {
			alert("search function");
			var keywords = document.<@liferay_portlet["namespace"] />fm.<@liferay_portlet["namespace"] />keywords.value;
	
			keywords = keywords.replace(/^\s+|\s+$/, '');
	
			if (keywords != '') {
				submitForm(document.<@liferay_portlet["namespace"] />fm);
			}
		}
	</@>
</@>

<#-- 
<div>
<ul>
<#list .data_model?keys as key>
 <li> ${key} </li>

</#list>
 </ul>
</div>
 -->
