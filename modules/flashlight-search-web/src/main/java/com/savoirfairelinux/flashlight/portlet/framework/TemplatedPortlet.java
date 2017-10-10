// Copyright 2017 Savoir-faire Linux
// This file is part of Flashlight Search.

// Flashlight Search is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Flashlight Search is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Flashlight Search.  If not, see <http://www.gnu.org/licenses/>.

package com.savoirfairelinux.flashlight.portlet.framework;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.portal.kernel.template.Template;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.template.TemplateException;
import com.liferay.portal.kernel.template.TemplateManager;
import com.liferay.portal.kernel.template.TemplateResource;
import com.liferay.portal.kernel.template.TemplateResourceLoader;
import com.liferay.portal.kernel.template.TemplateResourceLoaderUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portlet.display.template.PortletDisplayTemplate;
import com.savoirfairelinux.flashlight.portlet.framework.exception.CouldNotRenderTemplateException;
import com.savoirfairelinux.flashlight.portlet.framework.exception.TemplateNotFoundException;
import com.savoirfairelinux.flashlight.service.portlet.TemplateVariable;

/**
 * This type of portlets is used when the templating engine is something other than JSPs (for example, FreeMarker or
 * Velocity)
 */
public abstract class TemplatedPortlet extends GenericPortlet {

    public static final String INIT_PARAM_TEMPLATES_PATH = "templates-path";

    private static final String TEMPLATE_PATH_FORMAT = "%s" + TemplateConstants.SERVLET_SEPARATOR + "%s%s";

    private Portal portal;
    private String templatesPath;
    private TemplateManager templateManager;
    private TemplateResourceLoader templateResourceLoader;

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        this.templatesPath = config.getInitParameter(INIT_PARAM_TEMPLATES_PATH);

        if(this.templatesPath == null) {
            throw new PortletException("Templates path not specified in init parameters.");
        }

        if(!this.templatesPath.endsWith(StringPool.SLASH)) {
            this.templatesPath = this.templatesPath.concat(StringPool.SLASH);
        }

        this.portal = this.getPortal();
        this.templateManager = this.getTemplateManager();

        try {
            this.templateResourceLoader = TemplateResourceLoaderUtil.getTemplateResourceLoader(this.templateManager.getName());
        } catch(TemplateException e) {
            throw new PortletException(e);
        }
    }

    /**
     * Renders the given template
     *
     * @param request The request
     * @param response The response
     * @param templatePath The path of the template, relative to the templates path given in the init parameters
     *
     * @throws PortletException Thrown if there is an error during template processing
     * @throws IOException Thrown if there is an error while writing the response
     */
    public void renderTemplate(RenderRequest request, RenderResponse response, String templatePath) throws PortletException, IOException {
        this.renderTemplate(request, response, Collections.emptyMap(), templatePath);
    }

    /**
     * Renders the given template
     *
     * @param request The request
     * @param response The response
     * @param ctx The template context
     * @param templatePath The path of the template, relative to the templates path given in the init parameters
     *
     * @throws PortletException Thrown if there is an error during template processing
     * @throws IOException Thrown if there is an error while writing the response
     */
    public void renderTemplate(RenderRequest request, RenderResponse response, Map<String, Object> ctx, String templatePath) throws PortletException, IOException {
        PortletContext portletCtx = this.getPortletContext();
        String templateResourcePath = String.format(TEMPLATE_PATH_FORMAT, portletCtx.getPortletContextName(), this.templatesPath, templatePath);

        if(this.templateResourceLoader.hasTemplateResource(templateResourcePath)) {
            try {
                this.writeTemplate(request, response, ctx, templateResourcePath);
            } catch(TemplateException e) {
                throw new PortletException(e);
            }
        } else {
            throw new TemplateNotFoundException(templateResourcePath);
        }
    }

    /**
     * Writes the template's rendering in the portlet response
     *
     * @param request The request
     * @param response The response
     * @param ctx The template context
     * @param templateResourcePath The template to write
     *
     * @throws IOException Thrown if something goes wrong when writing the response
     * @throws TemplateException Thrown if something goes wrong when processing the template
     */
    private void writeTemplate(RenderRequest request, RenderResponse response, Map<String, Object> ctx, String templateResourcePath) throws TemplateException, IOException {
        TemplateResource templateResource = this.templateResourceLoader.getTemplateResource(templateResourcePath);
        Template template = this.templateManager.getTemplate(templateResource, false);

        this.templateManager.addTaglibSupport(template, this.portal.getHttpServletRequest(request), this.portal.getHttpServletResponse(response));
        this.enrichTemplateContext(request, response, template);

        Set<Entry<String, Object>> contextObjects = ctx.entrySet();
        for(Entry<String, Object> obj : contextObjects) {
            template.put(obj.getKey(), obj.getValue());
        }

        template.processTemplate(response.getWriter());
    }

    /**
     * Renders an ADT.
     *
     * @param request the request
     * @param response the response
     * @param templateCtx the template context
     * @param templateUUID the UUID of the ADT (DDMTemplate object/table) to render
     * @throws TemplateNotFoundException if no template having such UUID could be found
     * @throws CouldNotRenderTemplateException if the template could not be rendered
     */
    public void renderADT(RenderRequest request, RenderResponse response, Map<String, Object> templateCtx, String templateUUID) throws CouldNotRenderTemplateException, TemplateNotFoundException {
        HttpServletRequest httpServletRequest = this.portal.getHttpServletRequest(request);
        HttpServletResponse httpServletResponse = this.portal.getHttpServletResponse(response);
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        long companyId = themeDisplay.getCompanyId();

        try {
            List<DDMTemplate> ddmTemplates = this.getDDMTemplateLocalService().getDDMTemplatesByUuidAndCompanyId(templateUUID, companyId);
            if (ddmTemplates.isEmpty()) {
                throw new TemplateNotFoundException("No ADT found with companyId ["+companyId+"] and UUID ["+templateUUID+"]");
            }
            String renderedTemplate = this.getPortletDisplayTemplate().renderDDMTemplate(httpServletRequest, httpServletResponse, ddmTemplates.get(0), Collections.emptyList(), templateCtx);
            response.getWriter().write(renderedTemplate);
        } catch (Exception e) {
            throw new CouldNotRenderTemplateException("Could not render from ADT [" + templateUUID + "]", e);
        }
    }

    /**
     * Puts commonly used variables in the template context
     * @param request The request
     * @param response The response
     * @param template The template
     */
    private void enrichTemplateContext(RenderRequest request, RenderResponse response, Template template) {
        template.put(TemplateVariable.LOCALE.getVariableName(), request.getLocale());
        template.put(TemplateVariable.PORTLET_CONTEXT.getVariableName(), this.getPortletContext());
        template.put(TemplateVariable.REQUEST.getVariableName(), request);
        template.put(TemplateVariable.RESPONSE.getVariableName(), response);
        template.put(TemplateVariable.THEME_DISPLAY.getVariableName(), request.getAttribute(WebKeys.THEME_DISPLAY));
        template.put(TemplateVariable.USER_INFO.getVariableName(), request.getAttribute(PortletRequest.USER_INFO));
    }

    /**
     * @return The portal instance
     */
    protected abstract Portal getPortal();

    /**
     * @return The template manager
     */
    protected abstract TemplateManager getTemplateManager();

    /**
     * @return the display template service.
     */
    protected abstract PortletDisplayTemplate getPortletDisplayTemplate();

    /**
     * @return the DDM template service.
     */
    protected abstract DDMTemplateLocalService getDDMTemplateLocalService();

}
