package com.savoirfairelinux.flashlight.service.util;

/**
 * Just a little constant collections of pattern chunks frequently used in this application
 */
public class PatternConstants {

    /**
     * A pattern illustrating a class name
     */
    public static final String CLASS_NAME = "(([a-zA-Z0-9\\-_]\\.)*[a-zA-Z0-9\\-_]+)";

    public static final String UUID = "([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})";

    public static final String LOCALE = "(([a-z]{2})(_([A-Z]{2}))?)";

    /**
     * @throws Exception When constructed
     */
    private PatternConstants() throws Exception {
        throw new Exception("Constants class. Do not construct.");
    }

}
