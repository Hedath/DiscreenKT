package com.herolds.discreenkt.api.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class for accessing application wide configurations.
 * Created by Benedek Herold on 2017.07.15.
 */
public class ConfigProvider {

	private static final Logger logger = LoggerFactory.getLogger(ConfigProvider.class);

	private static final String MOVIE_LIST_URL_PATTERN_KEY = "movie_list_pattern_url";
	private static final String USER_URL_KEY = "user_url";
	private static final String MOVIEDB_API_KEY = "moviedb_api_key";
	private static final String MOVIEDB_POSTER_BASE_URL_KEY = "moviedb_poster_base_url";
	private static final String POSTER_DOWNLOAD_FOLDER_KEY = "poster_download_folder";
	private static final String MOVIE_CACHE_FOLDER_KEY = "movie_cache_folder";
	private static final String SYNC_INTERVAL = "sync_interval";
	private static final String SYNC_TIME = "sync_time";

	private static final String CONFIG_PROPERTIES_FILE = "config.properties";

	private Properties configProperties;

	private String defaultCacheFolder;
	
	private String appFolderPath;

	public ConfigProvider() {
		this.appFolderPath = getAppFolderPath();
		this.configProperties = loadConfig();		
		this.defaultCacheFolder = getCacheFolderPath();
	}
	
	public void configure(String configFilePath) {		
		Properties config = new Properties();
		try(FileInputStream configFileStream = new FileInputStream(configFilePath)) {
			config.load(configFileStream);
		} catch (Exception e) {
			logger.error("Error during configuration: ", e);
		}
		
		this.configProperties = config;
		this.defaultCacheFolder = getCacheFolderPath();
	}

	public void saveConfig() {
		try (FileWriter writer = new FileWriter(new File(getConfigFilePath()))) {
			configProperties.store(writer, "DiscreenKT config properties");
			logger.info("Wrote config to: {}", getConfigFilePath());
		} catch (IOException e) {
			logger.error("Could not write config to path", e);
		}
	}

	public Properties loadConfig() {
		configProperties = new Properties();
		
		Path configFilePath = Paths.get(getConfigFilePath());
		if (Files.exists(configFilePath)) {
			try(FileInputStream configFileStream = new FileInputStream(configFilePath.toFile())) {
				configProperties.load(configFileStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// Read default config
			InputStream configAsStream = ConfigProvider.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES_FILE);
			try {
				if (configAsStream != null) {
					configProperties.load(configAsStream);
				}
			} catch (IOException e) {
				logger.error("Error occured during config load: ", e);
				throw new RuntimeException(e);
			}			
		}
		
		return configProperties;
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
	
	private String getAppFolderPath() {
		if (appFolderPath != null) {
			return appFolderPath;
		}
		
		String appFolderLocation = "DiscreenKT";
    	
    	try {
			File tempFile = File.createTempFile("discreenkt", ".tmp");
			
			String tempFileAbsolutePath = tempFile.getAbsolutePath();
    		String tempFolderPath = tempFileAbsolutePath.substring(0, tempFileAbsolutePath.lastIndexOf(File.separator));
    		
    		appFolderLocation = tempFolderPath.concat(File.separator).concat(appFolderLocation);
    		
    		tempFile.delete();
		} catch (IOException | SecurityException e) {
			logger.error("Failed to get temp folder for app: ", e);
			appFolderLocation = new File(appFolderLocation).getAbsolutePath();
		}
    	
    	logger.info("App folder: {}", appFolderLocation);
    	
    	return appFolderLocation;
	}
	
	private String getCacheFolderPath() {
		return getAppFolderPath() + File.separator + "cache";
	}
	
	private String getConfigFilePath() {
		return getAppFolderPath() + File.separator + CONFIG_PROPERTIES_FILE;
	}
}
