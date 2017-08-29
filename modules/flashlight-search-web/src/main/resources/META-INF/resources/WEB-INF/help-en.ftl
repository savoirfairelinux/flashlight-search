<#include "init-help.ftl" />

<p>Click on a section to expand it.</p>

<h2><a id="${ns}user-guide" href="#">User guide</a></h2>
<div>
    <h3>Configuration</h3>
    <p>
        Flashlight performs search in tabs. Each tab contains a set of filters and rendering options.
        Tabs are <em>content-type specific</em> - meaning that only a single type of asset may be searched through a tab
    </p>
    <p>
        Prior to performing a search, you must at least create a search tab. To do so, simply go in the portlet's
        <strong>Edit</strong> or <strong>Preferences</strong> mode and create a <strong>tab</strong>.
    </p>
    <h3>Searching for DL File Entries (Documents and Medias)</h3>
    <p>
        While Flashlight has File Entry searching functionality built-in, it does not provide a way to render File Entry
        search results by default. Unlike Web content articles, file entries don't have templates. It is up to the user
        or developer to define how a file entry is represented in the search results.
    </p>
    <p>
        To do so, create an Application Display Template of type "DLFileEntry search result template". Once created, it
        will show up in the search tab's configuration panel.
    </p>
    <h3>Redefining the in-context Web content view</h3>
    <p>
        Like the Asset Publisher, Flahslight may render Web content articles directly inside the portlet if a content
        doesn't have a display page. If you wish to override that view, you may create an Application Display Template
        of type "Journal Article view Application Display Template".
    </p>
    <p>
        You may then, from the search tab's configuration, choose the Application Display Template to use when viewing
        the Web content articles from inside the search portlet.
    </p>
    <h3>Reusing Liferay built-in facets</h2>
    <p>
        Each tab can re-use some of Liferay's built-in facets to perform additional filtering. This functionality is
        experimental - not all Liferay facets may work as-is. Some facets must first be configured from Liferay's search
        portlet before they can be properly used from Flashlight. Some others simply don't work. Thourough testing of
        the search's behavior is recommended for each Liferay facet used from Flashlight's search portlet.
    </p>
</div>

<h2><a id="${ns}dev-guide" href="#">Developer guide</a></h2>
<div>
    <h3>Modifying the portlet's appearance through Application Display Templates</h2>
    <p>
        Flashlight's default portlet appearance is enterily realized using FreeMarker. Hence, the difference between the
        default template context and the application template context is minimal. If you wish to implement your own
        version of Flashlight's view, it is best to consult the portlet's source code in this regard.
    </p>
    <p>
        All of the portlet's logic is contained in its services and control layers. The view has everything it needs as
        template context variables to interact with the portlet. No service calls are needed from there.
    </p>
    <p>
        Flashlight makes use of Javascript to implement the "load more" functionality. Again, refer to the portlet's
        source code to know how to call the Portlet's "load more" functionality from Javascript code.
    </p>
    <h3>Integrating a search box in a Liferay theme</h2>
    <p>
       While it is possible to embed a portlet inside a theme, Flashlight provides the following request attribute that
       lets frontend developers assemble a search form directly from the theme:
    </p>
    <pre>com_savoirfairelinux_flashlight_portlet_FlashlightSearchPortlet_urls</pre>
    <p>
        Obtaining this request attribute from the theme will give you access to a SearchUrlContainer object. Here is a
        summary table of the object's functions:
    </p>
    <table class="table table-bordered">
        <thead>
            <tr><th>Type</th><th>Usage</th></tr>
        </thead>
        <tbody>
            <tr>
                <td>SearchUrlContainer</td>
                <td>
                    <p>Contains the available URLs in which Flashlight may be called</p>
                    <p>The following methods are available:</p>
                    <ul>
                        <li>
                            <strong>getSearchUrls()</strong> : Returns a map with, as keys, Layout objects in which the
                            Flashlight portlet resides. For each layout, a list of <strong>SearchUrl</strong> objects is
                            exposed as a value.
                        </li>
                        <li>
                            <strong>getSearchUrlByLayoutUuid(<em>String layoutUuid</em>)</strong> : Returns the list of
                            SearchUrl objects linked to the given Layout's UUID.
                        </li>
                    </ul>
                </td>
            </tr>
            <tr>
                <td>SearchUrl</td>
                <td>
                    <p>
                        Contains the URL of a layout containing the Flashlight portlet, along with a list of all the
                        request parameters that may be passed with the URL to perform an actual search. Whether the
                        parameters are put in a form or as a query string is up to the frontend developer.
                    </p>
                    <p>The following methods are available:</p>
                    <ul>
                        <li><strong>getLayout()</strong> : Returns the layout on which the portlet resides.</li>
                        <li><strong>getUrl()</strong> : Returns the URL, without parameters, to access the portlet.</li>
                        <li>
                            <strong>getRequestParameters()</strong> : Returns a list of
                            <strong>SearchUrlRequestParameter</strong> objects used to assemble the form or query string
                            to call the portlet. These are pre-filled request parameters and must be present for the
                            search to be triggered once on the search page.
                        </li>
                        <li>
                            <strong>getKeywordsParameter()</strong> : Returns the name of the parameter used to send the
                            search keywords.
                        </li>
                        <li>
                            <strong>getPortletNamespace()</strong> : Returns the portlet namespace
                        </li>
                    </ul>
                </td>
            </tr>
            <tr>
                <td>SearchUrlRequestParameter</td>
                <td>
                    <p>Holds a request parameter that may be added in a form or the query string.</p>
                    <p>The following methods are available:</p>
                    <ul>
                        <li><strong>getName()</strong> : The name of the request parameter</li>
                        <li><strong>getValue()</strong> : The value of the request parameter</li>
                    </ul>
                </td>
            </tr>
        </tbody>
    </table>
    <h3>Flashlight API Maven coordinates</h2>
    <p>
        Should you need to extend or override Flashlight's functionality, you may include the following dependency in
        your project:
    </p>
    <pre>&lt;groupId&gt;com.savoirfairelinux.liferay&lt;/groupId&gt;
&lt;artifactId&gt;flashlight-search-api&lt;/artifactId&gt;
&lt;version&gt;1.0.10&lt;/version&gt;</pre>
    <h2>Adding new content types</h2>
    <p>
        Any content type in Flashlight must be explicitly managed before they can be used in its search engine.
        Currently, the following content types are implemented:
    </p>
    <ol>
        <li>Journal Article (Web content)</li>
        <li>DL File Entry (Document Library)</li>
    </ol>
    <p>
        Each content type is managed through a Search Result Processor. The processor defines:
    </p>
    <ul>
        <li>The facets used to filter search results of the defined content type</li>
        <li>The way a search result is rendered in the search results list</li>
    </ul>
    <p>
        You may override or implement your own processor by deploying a <strong>SearchResultProcessor</strong>
        OSGi component. Once deployed, it will be added by Flashlight's service tracker. If you deploy a processor for
        a content type managed by Flashlight, it will be considered only if it has a higher service priority. Refer to
        the <strong>SearchResultProcessor</strong> documentation.
    </p>
    <p>
        Note that if a <strong>SearchResultProcessor</strong> fails to render a search result (for example, due to a
        template error), it will be omitted from the search result list by Flashlight. However, this behavior
        <strong>must not</strong> be used to filter search results as the search result count is extracted from the
        search facets. If you need to filter search results, use search facets.
    </p>
</div>