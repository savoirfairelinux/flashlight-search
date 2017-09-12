# Flashlight search system #

Flashlight is a set of OSGi modules aiming to provide a more personalized search experience inside Liferay 7/DXP.

## Installing Flashlight from source ##

You can produce the JARs by performing the following command in the source tree:

`mvn -C clean package`

This will produce OSGi bundles in each of the modules' `target` directory. Deploy them in Liferay's `deploy` folder.

## Using and extending Flashlight Search ##

After deploying all the Flashlight modules on your Liferay instance, you will be able to access the system's
documentation by placing a Flashlight Search Portlet on a page and accessing its "Help" mode.