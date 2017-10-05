<#include "init.ftl">

<header class="header-toolbar">
    <div class="toolbar-group-content">
        <a href="${redirect}"><span class="icon-angle-left"></span><span class="back-to-results"><@liferay_ui["message"] key="action.back.to.search.results" /></span></a>
    </div>
    <div class="toolbar-group-expand-text"></div>
</header>

<h2>${renderer.getTitle(locale)}</h2>

<@liferay_ui["asset-display"] assetEntry=entry assetRenderer=renderer assetRendererFactory=rendererFactory />
