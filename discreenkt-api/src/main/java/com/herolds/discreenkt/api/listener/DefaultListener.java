package com.herolds.discreenkt.api.listener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.listener.events.BaseEvent;
import com.herolds.discreenkt.api.listener.events.ErrorEvent;
import com.herolds.discreenkt.api.listener.events.FinishEvent;
import com.herolds.discreenkt.api.listener.events.PageParseEvent;
import com.herolds.discreenkt.api.listener.events.PosterDownloadEvent;
import com.herolds.discreenkt.api.listener.events.StartEvent;
import com.herolds.discreenkt.api.listener.events.StartPosterDownloadsEvent;

/**
 * Created by herold on 2018. 02. 03..
 */
public class DefaultListener implements DiscreenKTListener{
    
	private final Logger logger = LoggerFactory.getLogger(DefaultListener.class);
	
	@Override
    public void onStart(StartEvent event) {
    	logger.info("Starting poster synchronization. Number of pages: {}", event.getNumberOfPages());
    	logEventMessage(event);
    }

    @Override
    public void onPosterDownload(PosterDownloadEvent event) {
    	logger.info("Poster downloading for movie: {} (Success: {})", event.getMovieTitle(), event.isSuccess());
    	logEventMessage(event);
    }

    @Override
    public void onError(ErrorEvent event) {
    	logger.warn("Error during poster synchronization.");
    	logEventMessage(event);
    }

    @Override
    public void onFinish(FinishEvent event) {
    	logger.info("Finished poster synchronization, succesful count: {}", event.getSuccessCount());
    	logEventMessage(event);
    }

	@Override
	public void onPageParse(PageParseEvent event) { 
		logger.info("Parsed page: {}", event.getPageNumber());
		logEventMessage(event);
	}

	@Override
	public void onStartPosterDownloads(StartPosterDownloadsEvent event) {
		logger.info("Starting poster downloads, number of movies: {}", event.getNumberOfMovies());
		logEventMessage(event);
	}
	
	private void logEventMessage(BaseEvent event) {
		if (StringUtils.isNotEmpty(event.getMessage())) {
			logger.info("{} message: {}", event.getClass().getName(), event.getMessage());
		}
	}
}
