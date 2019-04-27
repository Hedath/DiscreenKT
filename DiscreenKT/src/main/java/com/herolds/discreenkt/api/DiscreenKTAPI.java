package com.herolds.discreenkt.api;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.herolds.discreenkt.api.listener.DefaultListener;
import com.herolds.discreenkt.api.listener.DiscreenKTListener;
import com.herolds.discreenkt.api.listener.events.BatchFinishedEvent;
import com.herolds.discreenkt.api.listener.events.ErrorEvent;
import com.herolds.discreenkt.api.listener.events.FinishEvent;
import com.herolds.discreenkt.api.listener.events.PosterDownloadEvent;
import com.herolds.discreenkt.api.listener.events.StartEvent;
import com.herolds.discreenkt.config.ConfigProvider;
import com.herolds.discreenkt.data.Movie;
import com.herolds.discreenkt.service.MovieCache;
import com.herolds.discreenkt.service.MovieListParser;
import com.herolds.discreenkt.service.MoviePosterManager;
import com.herolds.discreenkt.service.exception.DiscreenKTException;

/**
 * Created by herold on 2018. 01. 27..
 */
public class DiscreenKTAPI implements DiscreenKTListener {

    private final DiscreenKTListener listener;
    
    private final MoviePosterManager moviePosterManager;

    public DiscreenKTAPI(DiscreenKTListener listener, Properties properties) {
        if (listener == null) {
            this.listener = new DefaultListener();
        } else {
            this.listener = listener;
        }

        ConfigProvider.initConfigProvider(properties);
        this.moviePosterManager = new MoviePosterManager(this);
    }

    public void startDownload(Properties properties) {
        if (properties != null) {
            ConfigProvider.initConfigProvider(properties);
        }
        
        try {
            MovieListParser movieListParser = new MovieListParser();
            List<Movie> movies = movieListParser.getMovieLinks(ConfigProvider.getInstance().getMovieListUrl());

            moviePosterManager.processMovieList(movies);
        } catch (DiscreenKTException e) {
            onError(e.getErrorEvent());
        }
        catch (Exception e) {
            e.printStackTrace();
            ErrorEvent event = new ErrorEvent("Internal error! Please contact support services.");
            onError(event);
        }
    }
    
    public Optional<Instant> getLastSynchronization() {
    	return MovieCache.getInstance().getLastSynchronization();
    }
    
    public void exit() {
    	MovieCache.getInstance().close();
    }

    @Override
    public void onStart(StartEvent event) {
        listener.onStart(event);
    }

    @Override
    public void onPosterDownload(PosterDownloadEvent event) {
        listener.onPosterDownload(event);
    }

    @Override
    public void onBatchFinished(BatchFinishedEvent event) {
        listener.onBatchFinished(event);
    }

    @Override
    public void onError(ErrorEvent event) {
        listener.onError(event);
    }

    @Override
    public void onFinish(FinishEvent event) {
        listener.onFinish(event);
    }
}
