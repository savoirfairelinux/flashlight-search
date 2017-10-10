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
<#include "init.ftl">

<header class="header-toolbar">
    <div class="toolbar-group-content">
        <a href="${redirect}"><span class="icon-angle-left"></span><span class="back-to-results"><@liferay_ui["message"] key="action.back.to.search.results" /></span></a>
    </div>
    <div class="toolbar-group-expand-text"></div>
</header>

<h2>${renderer.getTitle(locale)}</h2>

<@liferay_ui["asset-display"] assetEntry=entry assetRenderer=renderer assetRendererFactory=rendererFactory />
