package com.herolds.discreenkt.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.config.ConfigProvider;
import com.herolds.discreenkt.api.data.Movie;
import com.herolds.discreenkt.api.listener.DefaultListener;
import com.herolds.discreenkt.api.listener.DiscreenKTListener;
import com.herolds.discreenkt.api.listener.events.ErrorEvent;
import com.herolds.discreenkt.api.listener.events.PageParseEvent;
import com.herolds.discreenkt.api.listener.events.StartEvent;
import com.herolds.discreenkt.api.service.DiscreenKTCache;
import com.herolds.discreenkt.api.service.MovieListParser;
import com.herolds.discreenkt.api.service.MoviePosterManager;
import com.herolds.discreenkt.api.service.exception.DiscreenKTException;

/**
 * Created by herold on 2018. 01. 27..
 */
public class DiscreenKTAPI {

	private final Logger logger = LoggerFactory.getLogger(DiscreenKTAPI.class);
	
	private MoviePosterManager moviePosterManager;
	
	private ConfigProvider configProvider;
	
	private MovieListParser movieListParser;
	
	private DiscreenKTCache discreenKTCache;
	
    private DiscreenKTListener listener;

    @Inject
    public DiscreenKTAPI(MoviePosterManager moviePosterManager, ConfigProvider configProvider, MovieListParser movieListParser, DiscreenKTCache discreenKTCache) {        
        this.moviePosterManager = moviePosterManager;
        this.configProvider = configProvider;
        this.movieListParser = movieListParser;
        this.discreenKTCache = discreenKTCache;
        
    	this.listener = new DefaultListener();
    }
    
    public void setListener(DiscreenKTListener listener) {
    	this.listener = listener;
    	this.moviePosterManager.setListener(listener);
    }

    public void startDownload() {
        try {
            int maxPage = movieListParser.getMaxPage(configProvider.getMovieListUrl());
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
            	
            	movies.addAll(movieListParser.getMovieLinks(configProvider.getMovieListUrl() + i));
            }
            
            logger.info("Processing parsed movies...");
            
            moviePosterManager.processMovieList(movies);	
        } catch (DiscreenKTException e) {
            listener.onError(e.getErrorEvent());
        } catch (Exception e) {
        	logger.error("Unexpected error", e);
        	
            ErrorEvent event = new ErrorEvent("Internal error! Please contact support services.");
            listener.onError(event);
        }
    }
    
    public Optional<Instant> getLastSynchronization() {
    	return discreenKTCache.getLastSynchronization();
    }
    
    public void exit() {
    	discreenKTCache.close();
    }
}
