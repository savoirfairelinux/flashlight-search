<#assign liferay_portlet = taglibLiferayHash["/META-INF/liferay-portlet.tld"] />
<#assign liferay_security = taglibLiferayHash["/META-INF/liferay-security.tld"] />
<#assign liferay_theme = taglibLiferayHash["/META-INF/liferay-theme.tld"] />
<#assign aui = taglibLiferayHash["/META-INF/liferay-aui.tld"] />

<#--  
<#if false>
<h1>Flashlight search</h1>
<@liferay_portlet["renderURL"] var="searchURL">
				<@liferay_portlet["param"] name="mvcPath" value="/search_result.ftl" />				
</@>
			
			<@aui["form"] action="${searchURL}" >
				<@aui["input"] name="keyword" label="" />
				
				<@aui["button"] type="submit" >
					<@liferay_ui["icon"] class="icon-monospaced" icon="search" markupView="lexicon"  />
				</@>
				
			</@>
</#if>
-->

<@liferay_portlet["renderURL"] var="searchURL">
	<@liferay_portlet["param"] name="mvcPath" value="/search_result.ftl" />				
</@>
<@aui["form"]  action="${searchURL}" class="form-inline" name="fm">
	<@aui["fieldset"]>
		<@aui["input"] class="search-input" label="" name="keywords" placeholder="search"  type="text" size="30" inlineField=true >
			<@aui["validator"] name="required" errorMessage="You must enter a search term"/>
		</@>
		<@aui["field-wrapper"] inlineField=true >
			<@aui["button"] type="submit"   value="Search" class="icon-monospaced " icon="icon-search"></@>
			<#if false>
			<@liferay_ui["icon"] class="icon-monospaced" icon="search" markupView="lexicon" onclick='alert("test");' inlineField=true url="javascript:;" />
			</#if>
		</@>
	</@>
	
<@aui["script"]>
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
