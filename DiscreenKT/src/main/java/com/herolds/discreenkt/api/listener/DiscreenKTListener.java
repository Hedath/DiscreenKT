package com.herolds.discreenkt.api.listener;

import com.herolds.discreenkt.api.listener.events.*;

/**
 * Created by herold on 2018. 02. 03..
 */
public interface DiscreenKTListener {

    void onStart(StartEvent event);
    void onPosterDownload(PosterDownloadEvent event);
    void onBatchFinished(BatchFinishedEvent event);
    void onError(ErrorEvent event);
    void onFinish(FinishEvent event);
}
