package com.herolds.discreenkt.api.listener;

import com.herolds.discreenkt.api.listener.events.ErrorEvent;
import com.herolds.discreenkt.api.listener.events.FinishEvent;
import com.herolds.discreenkt.api.listener.events.PageParseEvent;
import com.herolds.discreenkt.api.listener.events.PosterDownloadEvent;
import com.herolds.discreenkt.api.listener.events.StartEvent;
import com.herolds.discreenkt.api.listener.events.StartPosterDownloadsEvent;

/**
 * Created by herold on 2018. 02. 03..
 */
public interface DiscreenKTListener {

    default void onStart(StartEvent event) {}
    
    default void onPageParse(PageParseEvent event) {}
    
    default void onStartPosterDownloads(StartPosterDownloadsEvent event) {}
    
    default void onPosterDownload(PosterDownloadEvent event) {}
    
    default void onError(ErrorEvent event) {}
    
    default void onFinish(FinishEvent event) {}
   
}
