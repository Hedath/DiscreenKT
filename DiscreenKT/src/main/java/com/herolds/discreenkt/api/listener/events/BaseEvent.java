package com.herolds.discreenkt.api.listener.events;

/**
 * Created by herold on 2018. 02. 03..
 */
public abstract class BaseEvent {

    private String message;

    protected BaseEvent() {

    }

    protected BaseEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
