package com.herolds.discreenktgui.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Singleton class for accessing application wide configurations.
 * Created by Benedek Herold on 2017.07.15.
 *
 */
public class ConfigProvider {
    private static final String POSTER_DOWNLOAD_FOLDER_KEY = "poster_download_folder";
    private static final String MOVIE_CACHE_FOLDER_KEY = "movie_cache_folder";

    private static ConfigProvider instance;

    private final Properties configProperties;

    private ConfigProvider(Properties configProperties) {
        this.configProperties = configProperties;
    }

    public static ConfigProvider getInstance() {
        return instance;
    }

    public static void initConfigProvider(String configFilePath) throws IOException {
        Properties config = new Properties();
        try(FileInputStream configFileStream = new FileInputStream(configFilePath)) {
            config.load(configFileStream);
        }

        instance = new ConfigProvider(config);
    }

    public void writeConfig(String configFilePath) throws IOException {
        File file = new File(configFilePath);

        FileWriter writer = new FileWriter(file);

        configProperties.store(writer, "DiscreenKT config properties");
        writer.close();
    }

    public Properties loadConfig(String configFilePath) throws IOException {
        Properties config = new Properties();
        try(FileInputStream configFileStream = new FileInputStream(configFilePath)) {
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
