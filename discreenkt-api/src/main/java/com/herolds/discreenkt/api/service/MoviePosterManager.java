package com.herolds.discreenkt.api.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.config.ConfigProvider;
import com.herolds.discreenkt.api.data.Movie;
import com.herolds.discreenkt.api.listener.DefaultListener;
import com.herolds.discreenkt.api.listener.DiscreenKTListener;
import com.herolds.discreenkt.api.listener.events.FinishEvent;
import com.herolds.discreenkt.api.listener.events.PosterDownloadEvent;
import com.herolds.discreenkt.api.listener.events.StartPosterDownloadsEvent;
import com.herolds.discreenkt.api.service.exception.DiscreenKTException;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.methods.TmdbSearch;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;
import com.omertron.themoviedbapi.tools.HttpTools;

/**
 * Class for managing and downloading posters for movies.
 * The class is responsible for managing posters which means:
 * - If a movie was processed before, it shouldn't be processed again, only if the movie's poster image file cant be found.
 * - If a movie is no longer in the list, the poster should be removed
 * Created by h3r0ld on 2017. 07. 19..
 */
public class MoviePosterManager {

	private final Logger logger = LoggerFactory.getLogger(MoviePosterManager.class);
	
	private ConfigProvider configProvider;
	
	private DiscreenKTCache movieCache;
	
	private TmdbSearch tmdbSearch;

    private DiscreenKTListener listener;

    @Inject
    public MoviePosterManager(ConfigProvider configProvider, DiscreenKTCache movieCache) {
    	this.configProvider = configProvider;
    	this.movieCache = movieCache;
    	
    	this.listener = new DefaultListener();
	}

	public void setListener(DiscreenKTListener listener) {
		this.listener = listener;
	}

	public void processMovieList(List<Movie> movies) throws DiscreenKTException {
		HttpClient httpClient = HttpClientBuilder.create().build();
        this.tmdbSearch = new TmdbSearch(configProvider.getMovieDBApiKey(), new HttpTools(httpClient));
		
		List<Movie> postersToDownload = movies.stream()
        	.filter(this::isPosterNeeded)
        	.collect(Collectors.toList());
    	
    	listener.onStartPosterDownloads(StartPosterDownloadsEvent.builder()
        		.numberOfMovies(postersToDownload.size())
        		.build());

        List<PosterDownloadEvent> posterDownloadEvents = postersToDownload.stream()
                .map(this::downloadPoster)
                .collect(Collectors.toList());
        
        long successCount = posterDownloadEvents.stream()
        		.filter(PosterDownloadEvent::isSuccess)
        		.count();
        
        movieCache.putSynchronization(successCount);

        listener.onFinish(FinishEvent.builder()
        		.successCount(successCount)
        		.build());
    }

    private boolean isPosterNeeded(Movie movie) {
        return !movieCache.containsMovie(movie);
    }

    private PosterDownloadEvent downloadPoster(Movie movie) {
        PosterDownloadEvent event = new PosterDownloadEvent();
        event.setMovieTitle(movie.getTitle());

        Optional<MovieInfo> movieInfo = getMovieInfo(movie);

        if (movieInfo.isPresent() && movieInfo.get().getPosterPath() != null) {
        	logger.info("Downloaded movie poster: " + movie.getTitle());
            String baseUrl = configProvider.getMovieDBPosterBaseUrl();
            String outputPath = configProvider.getPosterDownloadFolder();

            String posterFileName = movieInfo.get().getPosterPath();

            String posterUrl = baseUrl + posterFileName;

            try (InputStream in = new URL(posterUrl).openStream()) {
                Files.copy(in, Paths.get(outputPath, posterFileName), StandardCopyOption.REPLACE_EXISTING);
                movieCache.putMovie(movie);
                event.setSuccess(true);
            } catch (IOException e) {
                logger.error("Exception while copying poster file", e);
                event.setSuccess(false);
            }
        } else {
            // TODO: Gather somewhere those movies for which it couldn't find a poster
        	logger.warn("Could not found movie poster: " + movie.getTitle());
        	event.setMessage("Not found");
            event.setSuccess(false);
        }

        listener.onPosterDownload(event);
        return event;
    }

    private Optional<MovieInfo> getMovieInfo(Movie movie) {
        try {
            ResultList<MovieInfo> movieSearchResult = tmdbSearch.searchMovie(movie.getTitle(), 1, null, false, movie.getYear(), null, null);

            logSearchResults(movie, movieSearchResult);

            if (!movieSearchResult.isEmpty()) {
                // TODO: Choose from results by title (exact match), or release year, if needed
                return Optional.of(movieSearchResult.getResults().get(0));
            }
        } catch (MovieDbException e) {
            logger.error("Exception while getting movies from tmdb: ", e);
        }

        return Optional.empty();
    }

    private void logSearchResults(Movie movie, ResultList<MovieInfo> movieSearchResult) {
    	logger.trace("----------------------------------");
        logger.trace("Search movie: " + movie.getTitle());
        logger.trace("\t Results: " + movieSearchResult.getTotalResults());
        for (MovieInfo movieInfo : movieSearchResult.getResults()) {
            logger.trace("--");
            logger.trace("\t\t Title: " + movieInfo.getTitle());
            logger.trace("\t\t Release: " + movieInfo.getReleaseDate());
            logger.trace("\t\t PosterPath: " + movieInfo.getPosterPath());
        }
    }
}
