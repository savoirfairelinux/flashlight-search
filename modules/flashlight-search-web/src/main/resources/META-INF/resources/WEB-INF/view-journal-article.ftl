<#include "init.ftl">

<header class="header-toolbar">
    <div class="toolbar-group-content">
        <a href="${redirect}"><span class="icon-angle-left"></span> ${renderer.getTitle(locale)}</a>
    </div>
    <div class="toolbar-group-expand-text"></div>
</header>

<@liferay_ui["asset-display"] assetEntry=entry assetRenderer=renderer assetRendererFactory=rendererFactory />
