package com.herolds.discreenkt.api.listener.events;

/**
 * Created by herold on 2018. 02. 03..
 */
public class StartEvent extends BaseEvent {

    private int numberOfMovies;

    public int getNumberOfMovies() {
        return numberOfMovies;
    }

    public void setNumberOfMovies(int numberOfMovies) {
        this.numberOfMovies = numberOfMovies;
    }
}
