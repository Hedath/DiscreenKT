package com.herolds.discreenkt.service;

import com.herolds.discreenkt.config.ConfigProvider;
import com.herolds.discreenkt.data.Movie;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.methods.TmdbSearch;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;
import com.omertron.themoviedbapi.tools.HttpTools;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Class for managing and downloading posters for movies.
 * The class is responsible for managing posters which means:
 *  - If a movie was processed before, it shouldn't be processed again, only if the movie's poster image file cant be found.
 *  - If a movie is no longer in the list, the poster should be removed
 * Created by h3r0ld on 2017. 07. 19..
 */
public class MoviePosterManager {
    private TmdbSearch tmdbSearch;

    public MoviePosterManager() {
        String apiKey = ConfigProvider.getInstance().getApiKey();
        HttpClient httpClient = HttpClientBuilder.create().build();
        this.tmdbSearch = new TmdbSearch(apiKey, new HttpTools(httpClient));
    }

    public void processMovieList(List<Movie> movies) {
        // TODO: Get MovieInfo for all movies, this was just to test

        for (Movie movie: movies) {
            // TODO: If it can't be found in the cache
            if (true) {
                MovieInfo movieInfo = getMovieInfo(movie);
                downloadPoster(movieInfo);
            }
        }


    }

    private MovieInfo getMovieInfo(Movie movie) {
        System.out.println("----------------------------------");
        System.out.println("Search movie: " + movie.getTitle());

        MovieInfo movieInfo = null;

        try {
            // TODO: We need to filter the movies more, because based on the title, the MovieInfo list can be quite long
            final ResultList<MovieInfo> movieSearchResult = tmdbSearch.searchMovie(movie.getTitle(), 1, null, false, null, null, null);


            if (!movieSearchResult.isEmpty()) {
                System.out.println("\t Results: " + movieSearchResult.getTotalResults());
                for (MovieInfo _movieInfo: movieSearchResult.getResults()) {
                    System.out.println("--");
                    System.out.println("\t\t Title: " + _movieInfo.getTitle());
                    System.out.println("\t\t PosterPath: " + _movieInfo.getPosterPath());
                }
                List<MovieInfo> results = movieSearchResult.getResults();
                movieInfo = results.get(0);
            }

        } catch (MovieDbException e) {
            e.printStackTrace();
        }

        return movieInfo;
    }

    private void downloadPoster(MovieInfo movieInfo) {
        if (movieInfo != null && movieInfo.getPosterPath() != null) {
            String baseUrl = ConfigProvider.getInstance().getPosterBaseUrl();
            String outputPath = ConfigProvider.getInstance().getOutputPath();

            String posterUrl = baseUrl + movieInfo.getPosterPath();

            try(InputStream in = new URL(posterUrl).openStream()){
                Files.copy(in, Paths.get(outputPath + movieInfo.getPosterPath().substring(1)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Gather somewhere those movies for which it couldn't find a poster
        }


    }
}
