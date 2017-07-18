package com.herolds.mpd.config;

import java.text.MessageFormat;

/**
 * Created by Benedek Herold on 2017.07.15.
 */
public class ConfigProvider {
    private static final String KT_URL = "https://kritikustomeg.org/user/{0}/filmek/?o=-other_rating_when";
    private static final String DAGNEROSS_URL = "12086/dagneross";

    public String getSiteUrl() {
        return KT_URL;
    }

    public String getUserUrl() {
        return DAGNEROSS_URL;
    }

    public String formatSiteUrl() {
        return MessageFormat.format(getSiteUrl(), getUserUrl());
    }

    // TODO: config file-ből jöjjenek ezek
}
