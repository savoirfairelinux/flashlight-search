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

package com.savoirfairelinux.flashlight.service.util;

/**
 * Just a little constant collections of pattern chunks frequently used in this application
 */
public class PatternConstants {

    /**
     * A pattern illustrating a class name
     */
    public static final String CLASS_NAME = "(([a-zA-Z0-9\\-_]+\\.)*[a-zA-Z0-9\\-_]+)";

    public static final String UUID = "([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})";

    public static final String LOCALE = "(([a-z]{2})(_([A-Z]{2}))?)";

    /**
     * @throws Exception When constructed
     */
    private PatternConstants() throws Exception {
        throw new Exception("Constants class. Do not construct.");
    }

}
