<#--
Copyright 2017 Savoir-faire Linux
This file is part of Flashlight Search.

Flashlight Search is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Flashlight Search is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Flashlight Search.  If not, see <http://www.gnu.org/licenses/>.
-->
<script type="text/javascript">

    function bindHelpButtons(namespace) {
        var buttons = new Array(2);
        buttons[0] = "user-guide";
        buttons[1] = "dev-guide";
        var buttonsLength = buttons.length;
        for(var i = 0; i < buttonsLength; i++) {
            var htmlButton = document.getElementById(namespace + buttons[i]);
            if(htmlButton) {
                htmlButton.parentNode.nextElementSibling.setAttribute('class', 'hidden');
                htmlButton.addEventListener('click', function(ev) {
                    var sibling = this.parentNode.nextElementSibling;
                    if(sibling.getAttribute('class') === 'hidden') {
                        sibling.removeAttribute('class');
                    } else {
                        sibling.setAttribute('class', 'hidden');
                    }
                    ev.preventDefault();
                    return false;
                });
            }
        }
    }

    if(typeof Liferay !== undefined) {
        Liferay.Portlet.ready(function(portletId, node) {
            var choppedNs = '${ns}';
            choppedNs = choppedNs.substring(1, choppedNs.length - 1);
            if(portletId === choppedNs) {
                bindHelpButtons('${ns}');
            }
        });
    } else {
        document.addEventListener('DOMContentLoaded', function(ev) {
            bindHelpButtons('${ns}');
        });
    }
</script>