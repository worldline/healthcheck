package net.atos.xa.healthcheck.openejb.util;

import java.net.URL;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class OpenEjbUtil {

	private static final String OPENEJB_CONTEXT_FACTORY = "org.apache.openejb.client.LocalInitialContextFactory";
	private static final String OPENEJB_CONF_PROPERTY = "openejb.configuration";
	private static final String OPENEJB_CONF_FILE = "openejb.xml";
	private static final String OPENEJB_LOGGING_CONF_FILE = "logging.properties";
	private static final String OPENEJB_LOGGING_CONF_PROPERTY = "logging.configuration";
	private static final String OPENEJB_CORE_LIB_NAME = "openejb-core";
	private static final String OPENEJB_EXTERNAL_LOGGER = "openejb.logger.external";

	public static Context startOpenEjb(String configFolderName)
			throws NamingException {
		Properties properties = new Properties();
		properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				OPENEJB_CONTEXT_FACTORY);

		properties.setProperty(OPENEJB_CONF_PROPERTY,
				searchForConfiguration(configFolderName, OPENEJB_CONF_FILE));

		properties.setProperty(
				OPENEJB_LOGGING_CONF_PROPERTY,
				searchForConfiguration(configFolderName,
						OPENEJB_LOGGING_CONF_FILE));

		System.setProperty(OPENEJB_EXTERNAL_LOGGER, "true");

		return new InitialContext(properties);
	}

	/**
	 * Search for configuration file : in current Properties, in conf folder of
	 * current directory, in conf folder of Tomcat, in DIR_CONFIG, in classpath.
	 * 
	 * @param fileName
	 *            name of the file we are looking for.
	 * @param propertyName
	 *            name of the associated property.
	 * @return file path or null if the file could not be found.
	 */
	private static String searchForConfiguration(String configFolderName,
			String filename) {

		// try to get the file stream from class path
		URL url = OpenEjbUtil.class.getClassLoader().getResource(
				configFolderName + "/" + filename);
		if (url != null) {
			String filePath = url.toString();
			// we ignore files packaged in OpenEJB itself
			if (!filePath.contains(OPENEJB_CORE_LIB_NAME)) {
				return filePath;
			}
		}
		return null;
	}

}
