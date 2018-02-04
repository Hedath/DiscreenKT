package com.herolds.discreenkt.api.listener.events;

/**
 * Created by herold on 2018. 02. 03..
 */
public class ErrorEvent extends BaseEvent {

    public ErrorEvent(String message) {
        super(message);
    }
}
