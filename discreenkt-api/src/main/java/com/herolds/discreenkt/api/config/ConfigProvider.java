package com.herolds.discreenkt.api.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.service.MovieCache;

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
	private static final String SYNC_INTERVAL = "sync_interval";
	private static final String SYNC_TIME = "sync_time";

	private static final String CONFIG_PROPERTIES_FILE = "config.properties";

	private static final Logger logger = LoggerFactory.getLogger(ConfigProvider.class);

	private static ConfigProvider instance;

	private final Properties configProperties;
	
	private final String defaultCacheFolder;

	private ConfigProvider(Properties configProperties) {
		this.configProperties = configProperties;
		this.defaultCacheFolder = getTempFolderPath();
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

	public static ConfigProvider initConfigProvider() {
		Properties config = new Properties();

		InputStream configAsStream = ConfigProvider.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES_FILE);

		try {
			config.load(configAsStream);
		} catch (IOException e) {
			logger.error("Error occured during config load: ", e);
			throw new RuntimeException(e);
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
		String movieCacheFolder = getProperty(MOVIE_CACHE_FOLDER_KEY); 
		
		if (StringUtils.isEmpty(movieCacheFolder)) {
			return defaultCacheFolder;
		} else {
			return movieCacheFolder;			
		}
	}

	public void setMovieCacheFolder(String movieCacheFolder) {
		configProperties.setProperty(MOVIE_CACHE_FOLDER_KEY, movieCacheFolder);
	}
	
	public String getSyncInterval() {
		return getProperty(SYNC_INTERVAL);
	}
	
	public void setSyncInterval(String syncInterval) {
		configProperties.setProperty(SYNC_INTERVAL, syncInterval);
	}
	
	public String getSyncTime() {
		return getProperty(SYNC_TIME);
	}
	
	public void setSyncTime(String syncTime) {
		configProperties.setProperty(SYNC_TIME, syncTime);
	}

	private String getProperty(String key) {
		return configProperties.getProperty(key);
	}
	
	private String getTempFolderPath() {
		String cacheLocation = "DiscreenKT" + File.separator + "cache";
    	
    	try {
			File tempFile = File.createTempFile("discreenkt", ".tmp");
			
			String tempFileAbsolutePath = tempFile.getAbsolutePath();
    		String tempFolderPath = tempFileAbsolutePath.substring(0, tempFileAbsolutePath.lastIndexOf(File.separator));
    		
    		cacheLocation = tempFolderPath.concat(File.separator).concat(cacheLocation);
    		
    		tempFile.delete();
		} catch (IOException | SecurityException e) {
			logger.error("Failed to get temp folder: ", e);
			
			cacheLocation = new File(cacheLocation).getAbsolutePath();
		}
    	
    	return cacheLocation;
	}
}
