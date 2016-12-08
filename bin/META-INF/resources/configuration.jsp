<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib prefix="liferay-ddm" uri="http://liferay.com/tld/ddm" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />


<%@page import="com.liferay.portal.kernel.util.Constants" %>
<%@ page import="com.liferay.portal.kernel.util.GetterUtil" %>
<%@ page import="com.liferay.portal.kernel.util.StringPool" %>
<%@ page import="com.liferay.portal.kernel.util.PortalUtil" %>
<%@ page import="com.liferay.portal.kernel.search.Document" %>
<%@ page import="com.liferay.blogs.kernel.model.BlogsEntry" %>



<%
String displayStyle = GetterUtil.getString(portletPreferences.getValue("displayStyle", StringPool.BLANK));
long displayStyleGroupId = GetterUtil.getLong(portletPreferences.getValue("displayStyleGroupId", null), scopeGroupId);

String blogdisplayStyle = GetterUtil.getString(portletPreferences.getValue("blogdisplayStyle", StringPool.BLANK));
long blogdisplayStyleGroupId = GetterUtil.getLong(portletPreferences.getValue("blogdisplayStyleGroupId", null), scopeGroupId);
%>

<liferay-portlet:actionURL portletConfiguration="true" var="configurationURL" />
<liferay-portlet:renderURL portletConfiguration="<%= true %>" var="configurationRenderURL" />

<aui:form action="<%= configurationURL %>" method="post" name="fm">
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />

	<aui:fieldset label="ADT">
		<div class="display-template">

			
        <liferay-ddm:template-selector
            className="<%= Document.class.getName() %>"
            displayStyle="<%= displayStyle %>"
            displayStyleGroupId="<%= displayStyleGroupId %>"
            refreshURL="<%= configurationRenderURL %>"
            showEmptyOption="<%= true %>"
        />
		</div>
		<div class="display-template">

			
		</div>
		
	</aui:fieldset>
	
	<aui:fieldset label="Other">
		<p> Some other configuration stuff we don't need for the moment
		</p>
  
		
	</aui:fieldset>

	<aui:button-row>
		<aui:button type="submit" />
	</aui:button-row>
</aui:form>