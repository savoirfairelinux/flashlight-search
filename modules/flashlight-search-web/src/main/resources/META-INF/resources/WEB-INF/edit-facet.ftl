<p><strong>"${searchFacet.title}"</strong> <span><@liferay_ui["message"] key="facet for tab" /></span> <strong>"${tabId}"</strong></p>
<p><a href="${editTabUrl}">Back to tab edition</a></p>

<form action="${saveFacetConfigUrl}" method="POST">
    <input type="hidden" name="${ns}redirect-url" value="${redirectUrl}" />
    <input type="hidden" name="${ns}tab-id" value="${tabId}" />
    <input type="hidden" name="${ns}facet-class-name" value="${searchFacet.class.name}" />

    <#-- Here we include the facet's JSP config. Scary, I know. -->
    ${searchFacet.includeConfiguration(servletRequest, servletResponse)}

    <div class="form-group">
        <input class="btn btn-default" type="submit" name="${ns}submit" value="<@liferay_ui["message"] key="Submit" />" />
    </div>
</form>
