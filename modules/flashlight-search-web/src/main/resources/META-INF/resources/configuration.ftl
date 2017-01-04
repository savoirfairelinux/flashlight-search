<#assign liferay_portlet = taglibLiferayHash["/META-INF/liferay-portlet.tld"] />
<#assign liferay_security = taglibLiferayHash["/META-INF/liferay-security.tld"] />
<#assign liferay_theme = taglibLiferayHash["/META-INF/liferay-theme.tld"] />
<#assign aui = taglibLiferayHash["/META-INF/liferay-aui.tld"] />
<#assign liferay_ddm = taglibLiferayHash["/META-INF/resources/liferay-ddm.tld"] />
<div>
<h1>Some interesting configuration</h1>
</div>

<#if true>

<#--  
<@liferay_portlet["actionURL"] name="configurationURL" portletConfiguration=true var="configurationURL" />
-->
<@liferay_portlet["renderURL"] portletConfiguration=true var="configurationRenderURL" />
 
<@liferay_portlet["actionURL"] name="configurationURL" var="configurationURL" />
<@aui["form"] action="${configurationURL}" method="post" name="fm">
	<@aui["input"] name="cmd" type="hidden" value="update" />
	
	<#if true>
	<@aui["fieldset"] label="ADT">
		<div class="display-template">


			<@liferay_ddm["template-selector"]
				className="${Request.documentClassName}"
				displayStyle="${Request.displayStyle[0]}"
				displayStyleGroupId=Request.displayStyleGroupId[0]
				refreshURL="${configurationRenderURL}"
				showEmptyOption=true
			/>
		</div>
	</@>
	</#if>
	<@aui["fieldset"] label="Assets display">
	<#if Request.facets??  && true>
	<#list Request.facets?keys as key>
	
	
	<div class="display-template">

			<#--  original
			<@liferay_ddm["template-selector"]
				className="${key}"
				displayStyle=Request.displayStyle[key?counter]
				displayStyleGroupId=Request.displayStyleGroupId[key?counter]
				refreshURL="${configurationRenderURL}"
				showEmptyOption=true
			/>
			-->
			
			<@liferay_ddm["template-selector"]
				className="com.liferay.journal.model.JournalArticle"
				displayStyle=Request.displayStyle[key?counter]
				displayStyleGroupId=Request.displayStyleGroupId[key?counter]
				refreshURL="${configurationRenderURL}"
				showEmptyOption=true
				label="${Request.facets[key]}"
			/>
		</div>
	</#list>
	</#if>
	
	</div>
	</@>
	
	<#--  multiple select for facet selection -->
	<#if true >
	<#list Request.enabled_facets as sf>
	<p>${sf}</p>
	</#list>
	
	<@aui["select"] name="selected_facets" multiple=true>
		<#list Request.searchFacets as facet>
			<@aui["option"] value="${facet.className}" label="${facet.title}" selected=Request.enabled_facets?seq_contains(facet.className) />
		</#list>
	</@>
	
	</#if>
	
	

	<@aui["button-row"]>
		<@aui["button"] type="submit" />
	</@>
</@>
</#if>


