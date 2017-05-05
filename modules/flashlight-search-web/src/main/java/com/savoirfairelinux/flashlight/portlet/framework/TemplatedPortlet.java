package com.savoirfairelinux.flashlight.portlet.framework;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.template.Template;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.template.TemplateException;
import com.liferay.portal.kernel.template.TemplateManager;
import com.liferay.portal.kernel.template.TemplateResource;
import com.liferay.portal.kernel.template.TemplateResourceLoader;
import com.liferay.portal.kernel.template.TemplateResourceLoaderUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.savoirfairelinux.flashlight.portlet.framework.exception.TemplateNotFoundException;

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
     * @return An empty, writable template context
     */
    protected Map<String, Object> createTemplateContext() {
        return new HashMap<String, Object>();
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

}
