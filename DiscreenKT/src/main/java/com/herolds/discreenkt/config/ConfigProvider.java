package com.herolds.discreenkt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Properties;

import com.herolds.discreenkt.service.MovieCache;

/**
 * Singleton class for accessing application wide configurations.
 * Created by Benedek Herold on 2017.07.15.
 */
public class ConfigProvider {
    private static final String MOVIE_LIST_URL_PATTERN_KEY = "movie_list_pattern_url";
    private static final String USER_URL_KEY = "user_url";
    private static final String MOVIEDB_API_KEY = "moviedb_api_key";
    private static final String MOVIEDB_POSTER_BASE_URL_KEY = "moviedb_poster_base_url";
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
    
    public static ConfigProvider initConfigProvider(String configFilePath) throws IOException {
        Properties config = new Properties();
        try(FileInputStream configFileStream = new FileInputStream(configFilePath)) {
            config.load(configFileStream);
        }

        instance = new ConfigProvider(config);
        
        return instance;
    }

    public static ConfigProvider initConfigProvider(URI configFilePath) throws IOException {
        Properties config = new Properties();
        try(FileInputStream configFileStream = new FileInputStream(Paths.get(configFilePath).toFile())) {
            config.load(configFileStream);
        }

        instance = new ConfigProvider(config);
        
        return instance;
    }

    public static void initConfigProvider(Properties config) {
        instance = new ConfigProvider(config);
    }
    
    public void writeConfig(URI configFilePath) throws IOException {
        File file = Paths.get(configFilePath).toFile();

        try (FileWriter writer = new FileWriter(file)) {
        	configProperties.store(writer, "DiscreenKT config properties");        	
        }
        
        MovieCache.reInitialize();
    }

    public Properties loadConfig(URI configFilePath) throws IOException {
        Properties config = new Properties();
       
        try(FileInputStream configFileStream = new FileInputStream(Paths.get(configFilePath).toFile())) {
            config.load(configFileStream);
        }

        return  config;
    }

    public String getMovieListUrlPattern() {
        return getProperty(MOVIE_LIST_URL_PATTERN_KEY);
    }

    public String getUserUrl() {
        return getProperty(USER_URL_KEY);
    }
    
    public void setUserUrl(String userUrl) {
    	configProperties.setProperty(USER_URL_KEY, userUrl);
    }

    public String getMovieListUrl() {
        return MessageFormat.format(getMovieListUrlPattern(), getUserUrl());
    }

    public String getMovieDBApiKey() {
        return getProperty(MOVIEDB_API_KEY);
    }

    public String getMovieDBPosterBaseUrl() {
        return getProperty(MOVIEDB_POSTER_BASE_URL_KEY);
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

    private String getProperty(String key) {
        return configProperties.getProperty(key);
    }
}
