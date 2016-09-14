package com.globant.automation.trainings.webframework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.globant.automation.trainings.webframework.config.interfaces.IConfig;
import com.globant.automation.trainings.webframework.config.interfaces.IDriver;
import com.globant.automation.trainings.webframework.config.interfaces.IProxy;
import com.globant.automation.trainings.webframework.config.interfaces.IWebDriverConfig;
import com.globant.automation.trainings.webframework.logging.Logging;
import com.globant.automation.trainings.webframework.webdriver.Browser;
import com.globant.automation.trainings.webframework.utils.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;

import static java.lang.Thread.currentThread;

/**
 * Global Framework's configuration entry point.
 * <p>
 * Reads a <i>config.yml</i> file expected to be found in classpath (usually <i>src/test/resources</i> for a Maven module)
 *
 * @author Juan Krzemien
 */
public enum Framework implements IConfig, Logging {

    CONFIGURATION;

    private static final String CONFIG_FILE = "config.yml";

    private final IConfig config;

    Framework() {
        Thread.currentThread().setName("Framework-Thread");
        getLogger().info("Initializing Framework configuration...");
        setDriversDownloadDirectory();
        this.config = readConfig();
    }

    private void setDriversDownloadDirectory() {
        // Define WebDriver's driver download directory once!
        File tmpDir = Environment.isWindows() ? new File("C:/Temp") : new File("/tmp");
        if (!tmpDir.exists()) {
            try {
                Files.createDirectory(tmpDir.toPath());
            } catch (IOException e) {
                getLogger().error(e.getMessage());
            }
        }
        System.setProperty("wdm.targetPath", tmpDir.getAbsolutePath());
    }

    private IConfig readConfig() {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        IConfig configuration = null;
        InputStream configFile = currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
        try {
            configuration = om.readValue(configFile, Config.class);
        } catch (Exception e) {
            getLogger().error("Error parsing framework config!. Re-check!", e);
        }
        return configuration;
    }

    @Override
    public boolean isDebugMode() {
        return config.isDebugMode();
    }

    @Override
    public IWebDriverConfig WebDriver() {
        return config.WebDriver();
    }

    @Override
    public IDriver Driver(Browser browser) {
        return config.Driver(browser);
    }

    @Override
    public IProxy Proxy() {
        return config.Proxy();
    }

    @Override
    public Set<Browser> AvailableDrivers() {
        return config.AvailableDrivers();
    }

}