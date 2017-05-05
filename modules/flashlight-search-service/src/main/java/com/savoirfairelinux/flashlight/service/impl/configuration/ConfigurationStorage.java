package com.savoirfairelinux.flashlight.service.impl.configuration;

import java.io.IOException;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import com.savoirfairelinux.flashlight.portlet.configuration.FlashlightConfiguration;

/**
 * The configuration storage interface is used to read and write application configuration in a versionned manner
 */
public interface ConfigurationStorage {

    /**
     * Key in the portlet preferences that holds the version of the configuration
     */
    public static final String PORTLET_PREFERENCES_VERSION_KEY = "configuration-version";

    /**
     * Version to put in the portlet preferences if none are found
     */
    public static final String PORTLET_PREFERENCES_DEFAULT_VERSION = "1";

    /**
     * Reads the configuration stored in portlet preferences and returns a model object corresponding to the
     * configuration. Bear in mind that the configuration cannot be considered valid. For example, the configuration may
     * refer to inexisting database objects as the configuration is not kept in sync with every Liferay object it refers
     * to.
     *
     * @param preferences The portlet preferences
     * @return The configuration model
     */
    public FlashlightConfiguration readConfiguration(PortletPreferences preferences);

    /**
     * Writes the given configuration model into the configuration. No format validation is performed at this level. It
     * is the developer's responsibility to send data that is in the expected format.
     *
     * @param configuration The configuration model
     * @param preferences The portlet preferences
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public void writeConfiguration(FlashlightConfiguration configuration, PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

    /**
     * Migrates the portlet preferences to the format used by this configuration reader/writer
     *
     * @param preferences The portlet preferences to migrate
     *
     * @throws ReadOnlyException If the configuration is read only
     * @throws ValidatorException If the configuration is invalid
     * @throws IOException If the configuration fails to save
     */
    public void migrateConfiguration(PortletPreferences preferences) throws ReadOnlyException, ValidatorException, IOException;

}
