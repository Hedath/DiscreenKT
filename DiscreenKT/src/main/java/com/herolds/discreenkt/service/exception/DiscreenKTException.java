package com.herolds.discreenkt.service.exception;

import com.herolds.discreenkt.api.listener.DiscreenKTListener;
import com.herolds.discreenkt.api.listener.events.ErrorEvent;

/**
 * Created by herold on 2018. 02. 03..
 */
public class DiscreenKTException extends Throwable {
    private ErrorEvent errorEvent;

    public DiscreenKTException(ErrorEvent errorEvent) {
        this.errorEvent = errorEvent;
    }

    public DiscreenKTException(String errorMessage) {
        this.errorEvent = new ErrorEvent(errorMessage);
    }

    public ErrorEvent getErrorEvent() {
        return errorEvent;
    }

    public void setErrorEvent(ErrorEvent errorEvent) {
        this.errorEvent = errorEvent;
    }
}
