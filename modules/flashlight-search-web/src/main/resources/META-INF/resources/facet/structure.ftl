<div class="panel panel-default">
<div class="panel-heading">Structure
</div>
<div>
<#if Request.facetResults?? >
<@liferay_portlet["renderURL"] var="showURL">
	<@liferay_portlet["param"] name="mvcPath" value="/show_results.ftl" />			
</@>
<@aui["form"] action="${showURL}">
<ul>
<#list Request.facetResults?keys as key>
	<li>${key}  :  ${Request.facetResults[key].frequency} , ${Request.facetResults[key].term}</li>
</#list>
</ul>
</@>
</#if>
</div>
</div>