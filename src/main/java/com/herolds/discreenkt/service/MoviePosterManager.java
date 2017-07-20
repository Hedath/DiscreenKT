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
import java.net.MalformedURLException;
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
        getMovieInfoFromTmdb(movies.get(0));
    }

    private void getMovieInfoFromTmdb(Movie movie) {
        System.out.println("----------------------------------");
        System.out.println("Search movie: " + movie.getTitle());

        try {
            // TODO: We need to filter the movies more, because based on the title, the MovieInfo list can be quite long
            final ResultList<MovieInfo> movieSearchResult = tmdbSearch.searchMovie(movie.getTitle(), 1, null, false, null, null, null);
            System.out.println("\t Results: ");

            // TODO: Decide from which MovieInfo we want to download the poster.
            downloadPoster(movieSearchResult.getResults().get(0).getPosterPath());

            for (MovieInfo movieInfo: movieSearchResult.getResults()) {
                System.out.println("--");
                System.out.println("\t\t Title: " + movieInfo.getTitle());
                System.out.println("\t\t PosterPath: " + movieInfo.getPosterPath());
            }
        } catch (MovieDbException e) {
            e.printStackTrace();
        }
    }

    private void downloadPoster(String posterPath) {
        String baseUrl = ConfigProvider.getInstance().getPosterBaseUrl();
        String posterUrl = baseUrl + posterPath;

        try(InputStream in = new URL(posterUrl).openStream()){
            // TODO: Remove hard coded file output path.
            Files.copy(in, Paths.get("/Users/herold/Desktop/image.jpg"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
