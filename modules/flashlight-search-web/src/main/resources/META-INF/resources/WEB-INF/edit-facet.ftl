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
