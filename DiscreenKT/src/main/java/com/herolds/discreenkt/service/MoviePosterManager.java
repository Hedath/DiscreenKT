package com.herolds.discreenkt.service;

import com.herolds.discreenkt.api.listener.DiscreenKTListener;
import com.herolds.discreenkt.api.listener.events.FinishEvent;
import com.herolds.discreenkt.api.listener.events.PosterDownloadEvent;
import com.herolds.discreenkt.api.listener.events.StartEvent;
import com.herolds.discreenkt.config.ConfigProvider;
import com.herolds.discreenkt.data.Movie;
import com.herolds.discreenkt.service.exception.DiscreenKTException;
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
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Class for managing and downloading posters for movies.
 * The class is responsible for managing posters which means:
 * - If a movie was processed before, it shouldn't be processed again, only if the movie's poster image file cant be found.
 * - If a movie is no longer in the list, the poster should be removed
 * Created by h3r0ld on 2017. 07. 19..
 */
public class MoviePosterManager {

    private TmdbSearch tmdbSearch;
    private MovieCache movieCache;

    private final DiscreenKTListener listener;

    public MoviePosterManager(DiscreenKTListener listener) {

        this.listener = listener;

        String apiKey = ConfigProvider.getInstance().getMovieDBApiKey();
        HttpClient httpClient = HttpClientBuilder.create().build();
        this.tmdbSearch = new TmdbSearch(apiKey, new HttpTools(httpClient));

        movieCache = MovieCache.getInstance();
    }

    public void processMovieList(List<Movie> movies) throws DiscreenKTException {

        StartEvent startEvent = new StartEvent();

        startEvent.setNumberOfMovies(movies.size());

        listener.onStart(startEvent);

        movies.stream()
                .filter(this::isPosterNeeded)
                .forEach(this::downloadPoster);

        FinishEvent finishEvent = new FinishEvent();
        listener.onFinish(finishEvent);
    }

    private boolean isPosterNeeded(Movie movie) {
        return !movieCache.containsMovie(movie);
    }

    private void downloadPoster(Movie movie) {
        PosterDownloadEvent event = new PosterDownloadEvent();
        event.setMovieTitle(movie.getTitle());

        MovieInfo movieInfo = getMovieInfo(movie);

        if (movieInfo != null && movieInfo.getPosterPath() != null) {
            ConfigProvider configProvider = ConfigProvider.getInstance();
            String baseUrl = configProvider.getMovieDBPosterBaseUrl();
            String outputPath = configProvider.getPosterDownloadFolder();

            String posterFileName = movieInfo.getPosterPath();

            String posterUrl = baseUrl + posterFileName;

            try (InputStream in = new URL(posterUrl).openStream()) {
                Files.copy(in, Paths.get(outputPath, posterFileName), StandardCopyOption.REPLACE_EXISTING);
                movieCache.putMovie(movie);
                event.setSuccess(true);
            } catch (IOException e) {
                e.printStackTrace();
                event.setSuccess(false);
            }
        } else {
            // TODO: Gather somewhere those movies for which it couldn't find a poster
            event.setSuccess(false);
        }

        listener.onPosterDownload(event);
    }

    private MovieInfo getMovieInfo(Movie movie) {
        try {
            ResultList<MovieInfo> movieSearchResult = tmdbSearch.searchMovie(movie.getTitle(), 1, null, false, movie.getYear(), null, null);

            //logSearchResults(movie, movieSearchResult);

            if (!movieSearchResult.isEmpty()) {
                // TODO: Choose from results by title (exact match), or release year, if needed
                return movieSearchResult.getResults().get(0);
            }
        } catch (MovieDbException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void logSearchResults(Movie movie, ResultList<MovieInfo> movieSearchResult) {
        System.out.println("----------------------------------");
        System.out.println("Search movie: " + movie.getTitle());
        System.out.println("\t Results: " + movieSearchResult.getTotalResults());
        for (MovieInfo movieInfo : movieSearchResult.getResults()) {
            System.out.println("--");
            System.out.println("\t\t Title: " + movieInfo.getTitle());
            System.out.println("\t\t Release: " + movieInfo.getReleaseDate());
            System.out.println("\t\t PosterPath: " + movieInfo.getPosterPath());
        }
    }
}
