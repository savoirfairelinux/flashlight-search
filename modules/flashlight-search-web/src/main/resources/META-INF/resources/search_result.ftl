
<#assign liferay_portlet = taglibLiferayHash["/META-INF/liferay-portlet.tld"] />
<#assign liferay_portlet_ext = taglibLiferayHash["/META-INF/liferay-portlet-ext.tld"] />
<#assign liferay_ddm = taglibLiferayHash["/META-INF/resources/liferay-ddm.tld"] />


<#assign journalArticleLocalService = Request.journalArticleLocalService>
<#assign params = Request["com.liferay.portal.kernel.servlet.PortletServletRequest"] />
<#assign results=Request.searchResults />


<@liferay_ddm["template-renderer"] 
	className="${Request.documentClassName}"
	displayStyle=Request.renderRequest.getPreferences().getValue("displayStyle","")
	displayStyleGroupId=Request.displayStyleGroupId
	entries=results
>

<@liferay_portlet["renderURL"] var="viewURL">
	<@liferay_portlet["param"] name="mvcPath" value="/view.ftl" />
</@>
<@liferay_portlet["renderURL"] varImpl="searchURL"/>


<@liferay_aui["form"] action="${searchURL}" name="search_form" method="get">
	<@liferay_portlet_ext["renderURLParams"] varImpl="searchURL"/>
	 
	<@liferay_aui["input"] name="ddmStructureKey"  type="hidden" value=params.getParameter("ddmStructureKey")!"" />
	<@liferay_aui["input"] name="fileEntryTypeId" type="hidden"  value=params.getParameter("fileEntryTypeId")!""/>
	<@liferay_aui["input"] name="assetCategoryIds" type="hidden"  value=params.getParameter("assetCategoryIds")!""/>
	<@liferay_aui["input"] name="entryClassName" type="hidden"  value=params.getParameter("entryClassName")!""/>
	
	<@liferay_aui["input"] name="createDate" type="hidden"   value=params.getParameter("createDate")!"" />
	<@liferay_aui["input"] name="mvcPath" type="hidden" value="/search_result.ftl" />
	<@liferay_aui["fieldset"]>
		<@liferay_aui["input"] class="search-input" label="" name="keywords" placeholder="search"  type="text" size="30" inlineField=true  >
			<@liferay_aui["validator"] name="required" errorMessage="You must enter a search term"/>
		</@>
		<@liferay_aui["field-wrapper"]  inlineField=true >
			<@liferay_aui["button"] type="submit"   value="Search" class="icon-monospaced " icon="icon-search"></@>
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
							<#if (Request.groupedDocuments[term.term])?? && Request.enabled_facets?seq_contains(facet.className) >
								<li><@liferay_aui["a"] href="#" onClick="facetFilter(['${facet.fieldName}' , '${term.term}'])" >${Request.facets[term.term]} <span class="badge">${term.frequency}</span></@></li>
							</#if>
						</#list>
					</#list>
				</ul>
			</div>
		</div>
	</nav>
	<#assign current_year = .now?string.yyyy?number  >	
	<#assign years = [current_year..2000][0] />
	
	<#assign ddmStructureKey=params.getParameter("ddmStructureKey")!"" />
	<#assign fileEntryTypeId = params.getParameter("fileEntryTypeId")!"" />
	<#assign entryClassName = params.getParameter("entryClassName")!"" />
	<#assign showing = ddmStructureKey!="" || fileEntryTypeId!="" || entryClassName!=""/>
	<#if ddmStructureKey!="" || fileEntryTypeId!="" || entryClassName!="" >
	<div class="row">
		<div class="col-md-9"></div>
		<#if Request.enabled_facets?seq_contains("com.savoirfairelinux.facet.CreatedSearchFacet") >
			<div class="col-md-1">
				<@liferay_aui["select"] name="create" onChange="filterByYear(this)" label="Years">
					<@liferay_aui["option"] value="any" label="any" />
					<#list years as year >
						<@liferay_aui["option"] value=year label=year />
					</#list>
				</@>
			</div>
		</#if>
	<#--  
	<div class="col-md-2">
	<@liferay_aui["select"] name="categories" id="categories" label="Category" onChange="categoryFilter()">
		<@liferay_aui["option"] label="any" value="any" data={'any' : 'any'} />
		<#list Request.searchFacets as facet>
						
						<#list facet.facet.facetCollector.termCollectors as term>
							<#if (Request.groupedDocuments[term.term])?? && Request.enabled_facets?seq_contains(facet.className) >
								
								<@liferay_aui["option"] label="${Request.facets[term.term]}" data={"key": facet.fieldName , "value" : term.term} key="${facet.fieldName}" value="${term.term}"  />
							</#if>
						</#list>
					</#list>
	</@>
	</div>
	-->
		<#if Request.enabled_facets?seq_contains("com.liferay.portal.search.web.internal.facet.AssetCategoriesSearchFacet") >
			<div class="col-md-2">
				<@liferay_aui["select"] name="vocabulary" id="vocabulary" label="Vocabulary" onChange="vocabularyFilter(this)">
					<@liferay_aui["option"] label="any" value=""  />
					<#list Request.categories as category>
						<@liferay_aui["option"] label=category.name value=category.categoryId />
					</#list>
				</@>
			</div>
		</#if>
	</#if>
		<@liferay_aui["script"]>
		function filterByYear(select){
			if(select.value != "any"){
				var range = "["+select.value+"0101000000 TO "+select.value+"1231235959 ]";
				$("#<@liferay_portlet["namespace"] />createDate").val(range);
			}
			else{
			
				$("#<@liferay_portlet["namespace"] />createDate").val("");
			}
			$("#<@liferay_portlet["namespace"] />search_form").submit();
			
		}
		function facetFilter(facet){
		//alert(facet[0] + " : " + facet[1]);
		$("#<@liferay_portlet["namespace"] />"+facet[0]).val(facet[1]);
		$("#<@liferay_portlet["namespace"] />search_form").submit();
		}
		function categoryFilter(){
			var key = $("#<@liferay_portlet["namespace"] />categories option:selected").attr("data-key");
			var value = $("#<@liferay_portlet["namespace"] />categories option:selected").attr("data-value");
			
			$("#<@liferay_portlet["namespace"] />"+key).val(value);
			$("#<@liferay_portlet["namespace"] />search_form").submit();
			//alert(select +"   " + select.dataset.key);
		}
		
		function vocabularyFilter(select){
			//alert(select.value);
			$("#<@liferay_portlet["namespace"] />assetCategoryIds").val(select.value);
			$("#<@liferay_portlet["namespace"] />search_form").submit();
		}
	</@>
	</div>
</@>

<div class="container">
	 <h2 class="h2">Default template</h2>
	 <div class="container">
		<#if results?has_content >
			<#list results as group>
				<#assign key = group.key />
				<div class="panel panel-default">
					<div class="panel-heading">
						${Request.facets[key]} <span class="badge">${group.documents?size}</span>
					</div>
					<div class="panel-body">
						<#assign groupdocuments = group.documents />
						<#if ddmStructureKey=="" && fileEntryTypeId=="">
							<#assign groupdocuments = group.documents[0..*3] />
						</#if>
						<#list groupdocuments as document>
							<#if document?index%3==0 >
							<div class="row">
							</#if>
							<div class="col-md-4"> 
								<#assign entryUrl = Request.flashlightUtil.getAssetViewURL(Request.renderRequest, Request.renderResponse, document)>
								<#if document.entryClassName == "com.liferay.journal.model.JournalArticle">
									<#assign article = journalArticleLocalService.fetchArticle(document.groupId?number, document.articleId)>
									<#assign template = Request.renderRequest.getPreferences().getValue("ddm-"+document.get("ddmStructureKey"), document.get(ddmTemplateKey))>
									<#assign content = journalArticleLocalService.getArticleContent(article, template, "VIEW", Request.themeDisplay.locale, Request.portletRequest, Request.themeDisplay)>
									${content?replace("{entryUrl}", entryUrl)}
								<#else>
								<h2><a href="${entryUrl}">${document.get("title")}</a></h2>
									<p>can't display ${document.entryClassName}</p>
								</#if>
							</div>
							
							<#if document?index%3==2 >
								</div>
							</#if>
						</#list>
						<#if groupdocuments?size%3 !=0>
							</div>
						</#if>
					</div>	
				</div>
			</#list>
		</#if>
	</div>
	<hr/>
	<div>
		<a href="${viewURL}" class="btn">Return to search</a>
	</div>
</@>	
</div>

