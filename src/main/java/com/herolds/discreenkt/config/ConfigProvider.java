package com.herolds.discreenkt.config;

import java.text.MessageFormat;

/**
 * Singleton class for accessing application wide configurations.
 * Created by Benedek Herold on 2017.07.15.
 */
public class ConfigProvider {
    private final String KT_URL = "https://kritikustomeg.org/user/{0}/filmek/?o=-other_rating_when";
    private final String DAGNEROSS_URL = "12086/dagneross";
    private final String API_KEY = "41a3fcf848f1ed1af18b18e5020d7f5b";
    private final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500/";
    private final String OUTPUT_PATH = "/Users/herold/Desktop/";

    private static ConfigProvider instance;

    private ConfigProvider() {}

    public static ConfigProvider getInstance() {
        if (instance == null) {
            instance = new ConfigProvider();
        }
        return instance;
    }

    public String getSiteUrl() {
        return KT_URL;
    }

    public String getUserUrl() {
        return DAGNEROSS_URL;
    }

    public String getApiKey() {
        return  API_KEY;
    }

    public String getPosterBaseUrl() {
        return POSTER_BASE_URL;
    }

    public String getOutputPath() {
        return OUTPUT_PATH;
    }

    public String formatSiteUrl() {
        return MessageFormat.format(getSiteUrl(), getUserUrl());
    }

    // TODO: config file-ből jöjjenek ezek
}
