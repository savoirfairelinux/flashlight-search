// Copyright 2017 Savoir-faire Linux
// This file is part of Flashlight Search.

// Flashlight Search is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Flashlight Search is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Flashlight Search.  If not, see <http://www.gnu.org/licenses/>.

package com.savoirfairelinux.flashlight.service.configuration;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * This class is used to configure the search service itself in a global scope. These settings affect how the service
 * works, not how individual portlets are configured.
 */
@ExtendedObjectClassDefinition(category = "other", scope = Scope.SYSTEM)
@OCD(id = FlashlightSearchServiceConfiguration.PID)
public interface FlashlightSearchServiceConfiguration {

    public static final String PID = "com.savoirfairelinux.flashlight.service.configuration.FlashlightSearchServiceConfiguration";

    /**
     * @return True to enable the workaround that makes Application Display Templates work during the resource serving phase. Only use on old Liferay DXP versions.
     */
    @AD(
        name = "Enable resource serving phase Application Display Templates workaround",
        description = "On some Liferay versions, a workaround is needed to make Application Display Templates work correctly during the resource serving phase. On recent Liferay versions, this should be left off.",
        deflt = "false",
        required = false
    )
    public boolean enableServeResourceADTWorkaround();

}
