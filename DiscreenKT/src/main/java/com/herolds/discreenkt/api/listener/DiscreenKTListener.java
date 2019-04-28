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

    void onStart(StartEvent event);
    
    void onPageParse(PageParseEvent event);
    
    void onStartPosterDownloads(StartPosterDownloadsEvent event);
    
    void onPosterDownload(PosterDownloadEvent event);
    
    void onError(ErrorEvent event);
    
    void onFinish(FinishEvent event);
}
