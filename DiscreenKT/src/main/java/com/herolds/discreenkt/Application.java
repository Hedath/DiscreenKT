package com.herolds.discreenkt;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.herolds.discreenkt.api.DiscreenKTAPI;
import com.herolds.discreenkt.api.listener.DefaultListener;
import com.herolds.discreenkt.config.ConfigProvider;
import com.herolds.discreenkt.data.Movie;
import com.omertron.themoviedbapi.MovieDbException;

/**
 * Main application.
 * Created by Benedek Herold on 2017.07.15.
 */
public class Application {
    public static void main(String[] args) throws MovieDbException {
        try {
            parseArguments(args);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        downloadPosters();
    }

    private static void parseArguments(String[] args) throws IOException {
        Option configFileOption = new Option("c", "config", true, "Config properties file path");
        configFileOption.setRequired(true);

        Options options = new Options();
        options.addOption(configFileOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        String configFilePath = cmd.getOptionValue("config");
        ConfigProvider.initConfigProvider(configFilePath);
    }

    private static void downloadPosters() {
        DiscreenKTAPI api = new DiscreenKTAPI(new DefaultListener(), null);

        api.startDownload(null);
        api.exit();
    }

    private static void printMovie(Movie movie) {
        System.out.println(movie.getKTid() + "\t" +
                movie.getTitle() + "\t" +
                movie.getSecondaryTitle() + "\t" +
                movie.getLink() + "\t" +
                movie.getYear());
    }
}
