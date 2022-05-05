package com.lingjoin.elasticsearch.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lingjoin.elasticsearch.index.IctclasAnalysisPlugin;
import org.elasticsearch.env.Environment;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.nio.file.Path;

/**
 * The type Configuration.
 */
public class Configuration {

    private final String licenseCode;
    private final String userDict;
    private final boolean overWrite;

    /**
     * Instantiates a new Configuration.
     *
     * @param licenseCode licenseCode
     * @param userDict    userDict 用户词典名称
     * @param overWrite   overWrite 是否覆盖用户词典
     */
    @ConstructorProperties({"licenseCode", "userDict", "overWrite"})
    public Configuration(String licenseCode, String userDict, boolean overWrite) {
        this.licenseCode = licenseCode;
        this.userDict = userDict;
        this.overWrite = overWrite;
    }

    /**
     * Gets license code.
     *
     * @return the license code
     */
    public String getLicenseCode() {
        return licenseCode;
    }

    /**
     * Gets user dict.
     *
     * @return the user dict
     */
    public String getUserDict() {
        return userDict;
    }

    /**
     * Is over write boolean.
     *
     * @return the boolean
     */
    public boolean isOverWrite() {
        return overWrite;
    }

    private static Configuration getDefaultConfiguration() {
        return new Configuration("", "", false);
    }

    /**
     * Gets configuration.
     *
     * @param environment the environment
     * @return the configuration
     */
    public static Configuration getConfiguration(Environment environment) {
        Path config = Configuration.getPluginPath(environment).resolve("config.yml");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return Access.doPrivileged(() -> {
            try {
                return mapper.readValue(config.toFile(), Configuration.class);
            } catch (IOException e) {
                IctclasAnalysisPlugin.LOGGER.error(
                        "Cannot parse config file from {}, use default config, error msg: {}",
                        config.toFile(), e.getMessage(), e
                );
                return Configuration.getDefaultConfiguration();
            }
        });

    }

    /**
     * Gets user dictionary path.
     *
     * @param environment the environment
     * @param userDict    the user dict
     * @return the user dictionary path
     */
    public static Path getUserDictionaryPath(Environment environment, String userDict) {
        return Configuration.getPluginPath(environment).resolve(userDict);
    }

    /**
     * Gets plugin path.
     *
     * @param environment the environment
     * @return the plugin path
     */
    public static Path getPluginPath(Environment environment) {
        return environment.pluginsFile().resolve(IctclasAnalysisPlugin.PLUGIN_NAME);
    }

}