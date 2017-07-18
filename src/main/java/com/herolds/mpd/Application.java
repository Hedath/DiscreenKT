package com.herolds.mpd;

import com.herolds.mpd.config.ConfigProvider;
import com.herolds.mpd.data.Movie;
import com.herolds.mpd.service.MovieListParser;

import java.io.IOException;
import java.util.List;

/**
 * Created by Benedek Herold on 2017.07.15.
 */
public class Application {
    public static void main(String[] args) {
        ConfigProvider configProvider = new ConfigProvider();
        MovieListParser movieListParser = new MovieListParser();

        try {
            List<Movie> movies = movieListParser.getMovieLinks(configProvider.formatSiteUrl());
            movies.forEach(Application::printMovie);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printMovie(Movie movie) {
        System.out.println(movie.getTitle() + "\t" + movie.getSecondaryTitle() + "\t" + movie.getLink());
    }
}
