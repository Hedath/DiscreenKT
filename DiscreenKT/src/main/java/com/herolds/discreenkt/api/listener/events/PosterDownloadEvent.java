package com.herolds.discreenkt.api.listener.events;

import javafx.geometry.Pos;

/**
 * Created by herold on 2018. 02. 03..
 */
public class PosterDownloadEvent extends BaseEvent {
    private String movieTitle;
    private boolean success;

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
