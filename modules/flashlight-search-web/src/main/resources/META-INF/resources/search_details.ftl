<#assign liferay_portlet = taglibLiferayHash["/META-INF/liferay-portlet.tld"] />
<#assign liferay_portlet_ext = taglibLiferayHash["/META-INF/liferay-portlet-ext.tld"] />
<#assign liferay_security = taglibLiferayHash["/META-INF/liferay-security.tld"] />
<#assign liferay_theme = taglibLiferayHash["/META-INF/liferay-theme.tld"] />
<#assign liferay_aui = taglibLiferayHash["/META-INF/liferay-aui.tld"] />
<#assign liferay_ddm = taglibLiferayHash["/META-INF/resources/liferay-ddm.tld"] />
<#assign liferay_ui = taglibLiferayHash["/META-INF/liferay-ui.tld"] />

<#assign Field = staticUtil["com.liferay.portal.kernel.search.Field"] />
<#assign content=Field.CONTENT />
<#assign title = Field.TITLE />
<#assign snippet = Field.SNIPPET>
<#assign tags = Field.ASSET_TAG_NAMES />
<#assign params = Request["com.liferay.portal.kernel.servlet.PortletServletRequest"] />
<#assign journalArticleLocalService = serviceLocator.findService("com.liferay.journal.service.JournalArticleLocalService")>
<#assign assetEntryLocalService = serviceLocator.findService("com.liferay.asset.kernel.service.AssetEntryLocalService")>

<@liferay_theme["defineObjects"] />
<@portlet["defineObjects"] />

<#assign results=Request.searchResults />
<@liferay_ddm["template-renderer"] 
				className="${Request.documentClassName}"
				displayStyle="${Request.displayStyle[0]}"
				displayStyleGroupId=Request.displayStyleGroupId[0]
				entries=Request.documents
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
	
	<@liferay_aui["input"] name="createDate" type="hidden"   value=params.getParameter("createDate")!"" />
	<@liferay_aui["input"] name="mvcPath" type="hidden" value="/search_details.ftl" />
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
								<@liferay_portlet["renderURL"] var="facetURL" varImpl="facetURL">
								<#--  original 
									<@liferay_portlet["param"] name="mvcPath" value="/search_result.ftl" />
									-->
									<@liferay_portlet["param"] name="mvcPath" value="/search_details.ftl" />
									<@liferay_portlet["param"] name="keywords" value=Request.keywords />
									<@liferay_portlet["param"] name=facet.fieldName value=term.term />
								</@>
								<#-- original code
								<li><a href="${facetURL}" >${Request.facets[term.term]} <span class="badge">${term.frequency}</span></a></li>
								 -->
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
	<div class="row">
	<div class="col-md-9">
	</div>
	
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
	</#if>
	</div>
		
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
	
		<div class="container">
			
<#if true>
		
			
				<h2 class="h2">Default template</h2>
			
			 <div class="container">
				<#if results?has_content >
					<#list results as group>
						<#assign key = group.key />
						<div class="panel panel-default">
							<div class="panel-heading">
								${Request.facets[key]}
							</div>
							 
							<div class="panel-body">
								<#list group.documents as document>
									<div class="col-md-4"> 
										<#assign assetEntry = assetEntryLocalService.getEntry(document.entryClassName, document.entryClassPK?number)>
										<#assign entryUrl = Request.assetPublisherHelper.getAssetViewURL(Request.renderRequest, Request.renderResponse, assetEntry, true)>
										<#if document.entryClassName == "com.liferay.journal.model.JournalArticle">
											<#assign article = journalArticleLocalService.fetchArticle(document.groupId?number, document.articleId)>  
											<#assign content = journalArticleLocalService.getArticleContent(article, "30345", "VIEW", locale, Request.portletRequest, themeDisplay)>
											${content?replace("{entryUrl}", entryUrl)}
										<#else>
											<p>can't display ${doc.entryClassName}</p>
										</#if>
									</div>
								</#list>
							</div>
							
						</div>
					</#list>
		
				</#if>
			</div>
			
		
	</#if>
			
		</div>
</@>	
	
	 
	<#--  
	<h2 class="h2">Grouped results</h2>
	
	<#if Request.groupedDocuments?has_content >
		<#list Request.facets?keys as key>
			<#if Request.groupedDocuments[key]??  && true>
				<#assign docs=Request.groupedDocuments[key] />
				<#-- original 
				<@liferay_ddm["template-renderer"] 
					className="${key}"
					displayStyle=Request.displayStyle[key?counter]
					displayStyleGroupId=Request.displayStyleGroupId[key?counter]
					entries=Request.groupedDocuments[key]
				>
				-->
				<#-- 
				<@liferay_ddm["template-renderer"] 
					className="com.liferay.journal.model.JournalArticle"
					displayStyle=Request.displayStyle[key?counter]
					displayStyleGroupId=Request.displayStyleGroupId[key?counter]
					entries=docs
				>
					<div class="panel panel-default">
						<div class="panel-heading">
							${Request.facets[key]}
						</div>
						<div class="panel-body">
							<#list docs as doc >
								<div class="col-md-12">
								<h2>${doc.title} (default )</h2>
								<p>${htmlUtil.escape(doc.content[0..*250])}</p>
								<#if doc.get("imageURL")??>
								<p><img src="${doc.get("imageURL")}" /></p>
								</#if>
								
								</div>
							</#list>
						</div>
					</div>
				</@>
			</#if>
		</#list>
	</#if>
-->
	<hr/>
	<div>
		<a href="${viewURL}" class="btn">Return to search</a>
	</div>
</div>