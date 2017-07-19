package com.herolds.discreenkt;

import com.herolds.discreenkt.config.ConfigProvider;
import com.herolds.discreenkt.data.Movie;
import com.herolds.discreenkt.service.MovieListParser;
import com.herolds.discreenkt.service.MoviePosterManager;
import com.omertron.themoviedbapi.MovieDbException;

import java.io.IOException;
import java.util.List;

/**
 * Main application.
 * Created by Benedek Herold on 2017.07.15.
 */
public class Application {
    public static void main(String[] args) throws MovieDbException {
        MovieListParser movieListParser = new MovieListParser();
        MoviePosterManager moviePosterManager = new MoviePosterManager();

        try {
            List<Movie> movies = movieListParser.getMovieLinks(ConfigProvider.getInstance().formatSiteUrl());
            movies.forEach(Application::printMovie);

            moviePosterManager.processMovieList(movies);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printMovie(Movie movie) {
        System.out.println(movie.getTitle() + "\t" + movie.getSecondaryTitle() + "\t" + movie.getLink());
    }
}
