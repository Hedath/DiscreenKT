package com.herolds.discreenkt.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.config.ConfigProvider;
import com.herolds.discreenkt.api.data.Movie;
import com.herolds.discreenkt.api.listener.DefaultListener;
import com.herolds.discreenkt.api.listener.DiscreenKTListener;
import com.herolds.discreenkt.api.listener.events.ErrorEvent;
import com.herolds.discreenkt.api.listener.events.FinishEvent;
import com.herolds.discreenkt.api.listener.events.PageParseEvent;
import com.herolds.discreenkt.api.listener.events.PosterDownloadEvent;
import com.herolds.discreenkt.api.listener.events.StartEvent;
import com.herolds.discreenkt.api.listener.events.StartPosterDownloadsEvent;
import com.herolds.discreenkt.api.service.DiscreenKTCache;
import com.herolds.discreenkt.api.service.MovieListParser;
import com.herolds.discreenkt.api.service.MoviePosterManager;
import com.herolds.discreenkt.api.service.exception.DiscreenKTException;

/**
 * Created by herold on 2018. 01. 27..
 */
public class DiscreenKTAPI implements DiscreenKTListener {

	Logger logger = LoggerFactory.getLogger(DiscreenKTAPI.class);
	
    private DiscreenKTListener listener;
    
    private final MoviePosterManager moviePosterManager;

    public DiscreenKTAPI() {        
        this.listener = new DefaultListener();
    	this.moviePosterManager = new MoviePosterManager(this);
    }
    
    public DiscreenKTAPI(DiscreenKTListener listener) {
    	this.listener = listener;
    	this.moviePosterManager = new MoviePosterManager(this);
    }
    
    public DiscreenKTAPI(DiscreenKTListener listener, Properties properties) {
        if (listener == null) {
            this.listener = new DefaultListener();
        } else {
            this.listener = listener;
        }

        if (properties != null) {
        	ConfigProvider.initConfigProvider(properties);        	
        }
        
        this.moviePosterManager = new MoviePosterManager(this);
    }
    
    public void registerListener(DiscreenKTListener listener) {
    	this.listener = listener;
    }

    public void startDownload() {
        try {
            MovieListParser movieListParser = new MovieListParser();
            
            int maxPage = movieListParser.getMaxPage(ConfigProvider.getInstance().getMovieListUrl());
            logger.info("Max pages to be downloaded: " + maxPage);
            
            List<Movie> movies = new ArrayList<>();
            

        	listener.onStart(StartEvent.builder()
            		.numberOfPages(maxPage)
            		.build());
            
            for(int i = 1; i <= maxPage; i++) {
            	logger.info("Current page: " + i);

            	listener.onPageParse(PageParseEvent.builder()
            			.pageNumber(i)
                		.build());
            	
            	movies.addAll(movieListParser.getMovieLinks(ConfigProvider.getInstance().getMovieListUrl() + i));
            }
            
            logger.info("Processing parsed movies...");
            
            moviePosterManager.processMovieList(movies);	
        } catch (DiscreenKTException e) {
            onError(e.getErrorEvent());
        } catch (Exception e) {
        	logger.error("Unexpected error", e);
        	
            ErrorEvent event = new ErrorEvent("Internal error! Please contact support services.");
            onError(event);
        }
    }
    
    public Optional<Instant> getLastSynchronization() {
    	return DiscreenKTCache.getInstance().getLastSynchronization();
    }
    
    public void exit() {
    	DiscreenKTCache.getInstance().close();
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
    public void onError(ErrorEvent event) {
        listener.onError(event);
    }

    @Override
    public void onFinish(FinishEvent event) {
        listener.onFinish(event);
    }

	@Override
	public void onPageParse(PageParseEvent event) {
		listener.onPageParse(event);
	}

	@Override
	public void onStartPosterDownloads(StartPosterDownloadsEvent event) {
		listener.onStartPosterDownloads(event);
	}
}
