package com.herolds.discreenkt;

import com.herolds.discreenkt.config.ConfigProvider;
import com.herolds.discreenkt.data.Movie;
import com.herolds.discreenkt.service.MovieCache;
import com.herolds.discreenkt.service.MovieListParser;
import com.herolds.discreenkt.service.MoviePosterManager;
import com.omertron.themoviedbapi.MovieDbException;

import java.util.List;

/**
 * Main application.
 * Created by Benedek Herold on 2017.07.15.
 */
public class Application {
    public static void main(String[] args) throws MovieDbException {
        try {
            MovieListParser movieListParser = new MovieListParser();
            List<Movie> movies = movieListParser.getMovieLinks(ConfigProvider.getInstance().formatSiteUrl());
//            movies.forEach(Application::printMovie);

            MoviePosterManager moviePosterManager = new MoviePosterManager();
            moviePosterManager.processMovieList(movies);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MovieCache.getInstance().close();
        }
    }

    private static void printMovie(Movie movie) {
        System.out.println(movie.getKTid() + "\t" +
                movie.getTitle() + "\t" +
                movie.getSecondaryTitle() + "\t" +
                movie.getLink() + "\t" +
                movie.getYear());
    }
}
