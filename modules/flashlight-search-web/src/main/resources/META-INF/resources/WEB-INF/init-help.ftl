<script type="text/javascript">

    function bindHelpButtons(namespace) {
        var buttons = new Array(2);
        buttons[0] = "user-guide";
        buttons[1] = "dev-guide";
        var buttonsLength = buttons.length;
        for(var i = 0; i < buttonsLength; i++) {
            var htmlButton = document.getElementById(namespace + buttons[i]);
            if(htmlButton) {
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