package com.herolds.discreenkt.api.listener;

import com.herolds.discreenkt.api.listener.events.ErrorEvent;
import com.herolds.discreenkt.api.listener.events.FinishEvent;
import com.herolds.discreenkt.api.listener.events.PosterDownloadEvent;
import com.herolds.discreenkt.api.listener.events.StartEvent;

/**
 * Created by herold on 2018. 02. 03..
 */
public class DefaultListener implements DiscreenKTListener{
    @Override
    public void onStart(StartEvent event) { }

    @Override
    public void onPosterDownload(PosterDownloadEvent event) { }

    @Override
    public void onError(ErrorEvent event) { }

    @Override
    public void onFinish(FinishEvent event) { }
}
