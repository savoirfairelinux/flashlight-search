<#assign liferay_portlet = taglibLiferayHash["/META-INF/liferay-portlet.tld"] />
<#assign liferay_portlet_ext = taglibLiferayHash["/META-INF/liferay-portlet-ext.tld"] />
<#assign liferay_security = taglibLiferayHash["/META-INF/liferay-security.tld"] />
<#assign liferay_theme = taglibLiferayHash["/META-INF/liferay-theme.tld"] />
<#assign aui = taglibLiferayHash["/META-INF/liferay-aui.tld"] />
<#assign liferay_ddm = taglibLiferayHash["/META-INF/resources/liferay-ddm.tld"] />
<#assign liferay_ui = taglibLiferayHash["/META-INF/liferay-ui.tld"] />

<#assign Field = staticUtil["com.liferay.portal.kernel.search.Field"] />
<#assign content=Field.CONTENT />
<#assign title = Field.TITLE />
<#assign snippet = Field.SNIPPET>
<#assign tags = Field.ASSET_TAG_NAMES />




<@liferay_portlet["renderURL"] var="viewURL">
	<@liferay_portlet["param"] name="mvcPath" value="/view.ftl" />
</@>
<@liferay_portlet["renderURL"] varImpl="searchURL"/>


<@aui["form"] action="${searchURL}" name="search_form" method="get">
	<@liferay_portlet_ext["renderURLParams"] varImpl="searchURL"/>
	<@aui["input"] name="mvcPath" type="hidden" value="/search_result.ftl" />
	<@aui["fieldset"]>
		<@aui["input"] class="search-input" label="" name="keywords" placeholder="search"  type="text" size="30" inlineField=true  >
			<@aui["validator"] name="required" errorMessage="You must enter a search term"/>
		</@>
		<@aui["field-wrapper"]  inlineField=true >
			<@aui["button"] type="submit"   value="Search" class="icon-monospaced " icon="icon-search"></@>
		</@>
	</@>

	<nav class="navbar navbar-inverse">
		<div class="container-fluid">
			<div class="collapse navbar-collapse">
				<ul class="nav navbar-nav">
					<@liferay_portlet["renderURL"] var="facetURL">
						<@liferay_portlet["param"] name="mvcPath" value="/search_result.ftl" />
						<@liferay_portlet["param"] name="keywords" value=Request.keywords />
					</@>
					<li><a href="${facetURL}" >All</a></li>
					<#list Request.searchFacets as facet>
						<#list facet.facet.facetCollector.termCollectors as term>
							<#if (Request.groupedDocuments[term.term])?? >
								<@liferay_portlet["renderURL"] var="facetURL">
									<@liferay_portlet["param"] name="mvcPath" value="/search_result.ftl" />
									<@liferay_portlet["param"] name="keywords" value=Request.keywords />
									<@liferay_portlet["param"] name=facet.fieldName value=term.term />
								</@>
								<li><a href="${facetURL}">${Request.facets[term.term]} <span class="badge">${term.frequency}</span></a></li>
							</#if>
						</#list>
					</#list>
				</ul>
			</div>
		</div>
	</nav>	
</@>	

<div class="container">
	<#if false>
		<div class="container">
			<@liferay_ddm["template-renderer"] 
				className="${Request.documentClassName}"
				displayStyle="${Request.displayStyle[0]}"
				displayStyleGroupId=Request.displayStyleGroupId[0]
				entries=Request.documents
			>
				<h2 class="h2">All results</h2>
				<#list Request.documents as doc>
					<div class="col-md-4">
						<h2>${doc.get(title)}</h2>
						<p>${doc.get(content)} </p>
						<p>asset type:  ${doc.get("entryClassName")}</p>
						<p>${doc.get(snippet)}</p>
						<p>tags: </p>
						<p><a class="btn btn-default" href="#" role="button">View details &raquo;</a></p>
					</div>
				</#list>
			</@>
		</div>
	</#if>
	<#-- 
	<div class="container">
	<h2>Test article display</h2>
	<div class="abderrahmane">
	${Request.articleDisplay.getContent()}
	</div>
	</div>
	 -->
	<h2 class="h2">Grouped results</h2>
	<#if Request.groupedDocuments?has_content >
		
		 <#--  
		<#list Request.facets?keys as key>
		<p>key</p>
		</#list>
		--> 
		<#list Request.facets?keys as key>
			<#if Request.groupedDocuments[key]??  && true>
				<#assign docs=Request.groupedDocuments[key][0..*3] />
				<#-- original 
				<@liferay_ddm["template-renderer"] 
					className="${key}"
					displayStyle=Request.displayStyle[key?counter]
					displayStyleGroupId=Request.displayStyleGroupId[key?counter]
					entries=Request.groupedDocuments[key]
				>
				-->
				
				<@liferay_ddm["template-renderer"] 
					className="com.liferay.journal.model.JournalArticle"
					displayStyle=Request.displayStyle[key?counter]
					displayStyleGroupId=20147
					entries=docs
				>
					<div class="panel panel-default">
						<div class="panel-heading">
							${Request.facets[key]}
						</div>
						<div class="panel-body">
							<#list docs as doc >
								<div class="col-md-4">
								<h2>${doc.title} (default)</h2>
								<p>${doc.content}</p>
								</div>
							</#list>
						</div>
					</div>
				</@>
			</#if>
			
			<#if Request.groupedDocuments[key]?? && false>
				<div class="panel panel-primary">
					<#--  		
					<div class="panel-heading"><@liferay_ui["message"] key="flashlight-${key}" /> <span class="badge">${Request.groupedDocuments[key]?size}</span></div>
					-->	
					
					<div class="panel-heading">${key} <span class="badge">${Request.groupedDocuments[key]?size}</span></div>
						<div class="panel-body">
							<#if true>
								<#list Request.groupedDocuments[key] as document>
									<div class="col-md-4">
										<h2><u>${document.get(title)}</u></h2>
										<p>${document.get(content)} <a href="">read more</a></p>
										
										<#--
										<p><a class="btn btn-default" href="#" role="button">View details &raquo;</a></p>
										-->
									</div>
								</#list>
							</#if>
					</div>
				</div>
			</#if>
		</#list>
	</#if>
	<hr/>
	<div>
		<a href="${viewURL}" class="btn">Return to search</a>
	</div>
</div>

