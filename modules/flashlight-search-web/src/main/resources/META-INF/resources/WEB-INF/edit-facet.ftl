<h2><@liferay_ui["message"] key="Facet configuration" /></h2>
<p><a href="${editTabUrl}">Back to tab edition</a></p>

<form>
    <#-- Here we include the facet's JSP config. Scary, I know. -->
    ${searchFacet.includeConfiguration(servletRequest, servletResponse)}
</form>
