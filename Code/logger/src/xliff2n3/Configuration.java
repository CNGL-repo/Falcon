package xliff2n3;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {

    /**
     * The Properties object holding the configuration information
     */
    private static Properties config;

    /**
     * Gets the configuration object (lazily initialized)
     * @return the configured Properties object holding configuration information
     */
    public static Properties getProperties() {
        if (config == null) {
            // Create once
            config = new Properties();
            try {
                config.load(Configuration.class.getResourceAsStream(Constants.CONFIG_FILENAME));
            } catch (IOException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return config;
    }

    /**
     * Convenience method to call getProperty on the wrapped Properties object
     * @param key the property key
     * @return the value in the property list with the specified key value
     */
    public static String getProperty(String key) {
        Properties properties = getProperties();
        return properties.getProperty(key);
    }
}
