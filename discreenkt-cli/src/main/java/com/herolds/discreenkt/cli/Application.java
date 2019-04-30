package com.herolds.discreenkt.cli;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.DiscreenKTAPI;
import com.herolds.discreenkt.api.config.ConfigProvider;

/**
 * Main application.
 * Created by Benedek Herold on 2017.07.15.
 */
public class Application {
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
    public static void main(String[] args) {
        try {
            parseArguments(args);
        } catch (IOException | ParseException e) {
        	logger.error("Error while parsing arguments: ", e);
            return;
        }

        downloadPosters();
    }

    private static void parseArguments(String[] args) throws IOException, ParseException {
        Option configFileOption = new Option("c", "config", true, "Config properties file path");
        configFileOption.setRequired(true);

        Options options = new Options();
        options.addOption(configFileOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
        	logger.error("Error while parsing command line: ", e);

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        String configFilePath = cmd.getOptionValue("config");
        ConfigProvider.initConfigProvider(configFilePath);
    }

    private static void downloadPosters() {
    	DiscreenKTAPI api = new DiscreenKTAPI();

        api.startDownload();
        api.exit();
    }
}
