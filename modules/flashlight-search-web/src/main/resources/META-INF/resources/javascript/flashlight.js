var com_savoirfairelinux_flashlight_portlet = com_savoirfairelinux_flashlight_portlet || {};

/**
 * Creates a client-side portlet connector with the given portlet namespace
 *
 * @param {String} portletNamespace The portlet namespace
 */
com_savoirfairelinux_flashlight_portlet.FlashlightSearchPortlet = function(portletNamespace) {
    this.portletNamespace = portletNamespace;
}

/**
 * Returns an HTML element by its portlet-namespaced ID
 *
 * @param {String} elementId The element's ID, without the portlet namespace
 * @return {HTMLElement} The HTML element corresponding to the portlet-namespaced ID or null if none found
 */
com_savoirfairelinux_flashlight_portlet.FlashlightSearchPortlet.prototype.getElementById = function(elementId) {
    return document.getElementById(this.portletNamespace + elementId);
}

/**
 * Returns an HTML element of the portlet
 *
 * @return {HTMLElement} The HTML element corresponding to the portlet
 */
com_savoirfairelinux_flashlight_portlet.FlashlightSearchPortlet.prototype.getPortletElement = function() {
    return document.getElementById('p_p_id' + this.portletNamespace);
}

/**
 * Binds the "load more" functionality to the given HTML element and event
 *
 * @param {HTMLElement} loadMoreElement The HTML element that can trigger the "load more" functionality
 * @param {String} triggerEventName The name of the event that triggers the "load more" functionality (for example, "click")
 * @param {String} urlAttribute The name of the HTML attribute containing the "load more" resource-serving URL
 * @param {function} progressCallback A function callback called when the XHR request progresses. Takes 2 parameters, in order : The progress event and the "load more" HTML element that was used
 * @param {function} successCallback A function callback called when the XHR request ends with success. Takes 3 parameters, in order : The load event, the "load more" HTML element and the JSON response payload object
 * @param {function} errorCallback A function callback called when the XHR request ends with an error. Takes 2 parameters, in order : The error event and the "load more" HTML element that was used
 * @param {function} abortedCallback A function callback called when the XHR request is aborted. Takes 2 parameters, in order : The abort event and the "load more" HTML element that was used
 */
com_savoirfairelinux_flashlight_portlet.FlashlightSearchPortlet.prototype.bindLoadMore = function(loadMoreElement, triggerEventName, urlAttribute, progressCallback, successCallback, errorCallback, abortedCallback) {
    var self = this;

    loadMoreElement.addEventListener(triggerEventName, function(event) {
        if(this.getAttribute("disabled") === null) {
            self._performQuery(this, urlAttribute, progressCallback, successCallback, errorCallback, abortedCallback);
        }
        event.preventDefault();
        return false;
    });
}

/**
 * Performs the "load more" query. This will:
 *
 * 1. Send an XHR to the resource serving phase for "load more"
 * 2. Disable the HTML element used to trigger the "load more"
 * 3. Send progress events
 * 4. Re-enable the HTML element used to trigger the "load more"
 * 5. Send error/canceled events if needed
 * 6. Send the success event if needed
 * 7. If no "load mores" are necessary from the search engine, disable the HTML element to trigger the "load more"
 *
 * @param {HTMLElement} loadMoreElement The HTML element that can trigger the "load more" functionality
 * @param {String} urlAttribute The name of the HTML attribute containing the "load more" resource-serving URL
 * @param {function} progressCallback A function callback called when the XHR request progresses. Takes 2 parameters, in order : The progress event and the "load more" HTML element that was used
 * @param {function} successCallback A function callback called when the XHR request ends with success. Takes 3 parameters, in order : The load event, the "load more" HTML element and the JSON response payload object
 * @param {function} errorCallback A function callback called when the XHR request ends with an error. Takes 2 parameters, in order : The error event and the "load more" HTML element that was used
 * @param {function} abortedCallback A function callback called when the XHR request is aborted. Takes 2 parameters, in order : The abort event and the "load more" HTML element that was used
 */
com_savoirfairelinux_flashlight_portlet.FlashlightSearchPortlet.prototype._performQuery = function(element, urlAttribute, progressCallback, successCallback, errorCallback, abortedCallback) {
    var xhrUrl = element.getAttribute(urlAttribute);
    element.setAttribute("disabled", "disabled");

    var xhr = new XMLHttpRequest();

    xhr.addEventListener("progress", function(event) {
        progressCallback(event, element);
    });

    xhr.addEventListener("load", function(event) {
        var jsonObj = JSON.parse(event.target.responseText);
        var loadMoreUrl = jsonObj.loadMoreUrl;
        if(loadMoreUrl) {
            element.setAttribute(urlAttribute, loadMoreUrl);
            element.removeAttribute("disabled");
        }
        successCallback(event, element, jsonObj);
    });
    xhr.addEventListener("error", function(event) {
        element.removeAttribute("disabled");
        errorCallback(event, element);
    });
    xhr.addEventListener("abort", function(event) {
        element.removeAttribute("disabled");
        abortedCallback(event, element);
    });

    xhr.open("GET", xhrUrl);
    xhr.send();
}
