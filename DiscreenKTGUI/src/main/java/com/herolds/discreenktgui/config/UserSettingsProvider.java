package com.herolds.discreenktgui.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Singleton class for accessing user provided settings.
 */
public class UserSettingsProvider {
    private static final String POSTER_DOWNLOAD_FOLDER_KEY = "poster_download_folder";
    private static final String MOVIE_CACHE_FOLDER_KEY = "movie_cache_folder";

    private static UserSettingsProvider instance;

    private final Properties configProperties;

    private UserSettingsProvider(Properties configProperties) {
        this.configProperties = configProperties;
    }

    public static UserSettingsProvider getInstance() {
        return instance;
    }

    public static void initConfigProvider(URI configFilePath) throws IOException {
        Properties config = new Properties();
        
        try(FileInputStream configFileStream = new FileInputStream(Paths.get(configFilePath).toFile())) {
            config.load(configFileStream);
        }

        instance = new UserSettingsProvider(config);
    }

    public void writeConfig(URI configFilePath) throws IOException {
        File file = Paths.get(configFilePath).toFile();

        FileWriter writer = new FileWriter(file);

        configProperties.store(writer, "DiscreenKT config properties");
        writer.close();
    }

    public Properties loadConfig(URI configFilePath) throws IOException {
        Properties config = new Properties();
        try(FileInputStream configFileStream = new FileInputStream(Paths.get(configFilePath).toFile())) {
            config.load(configFileStream);
        }

        return  config;
    }

    public String getPosterDownloadFolder() {
        return getProperty(POSTER_DOWNLOAD_FOLDER_KEY);
    }

    public void setPosterDownloadFolder(String posterDownloadFolder) {
        configProperties.setProperty(POSTER_DOWNLOAD_FOLDER_KEY, posterDownloadFolder);
    }

    public String getMovieCacheFolder() {
        return getProperty(MOVIE_CACHE_FOLDER_KEY);
    }

    public void setMovieCacheFolder(String movieCacheFolder) {
        configProperties.setProperty(MOVIE_CACHE_FOLDER_KEY, movieCacheFolder);
    }

    public Properties getConfigProperties() {
        return configProperties;
    }

    private String getProperty(String key) {
        return configProperties.getProperty(key);
    }
}
