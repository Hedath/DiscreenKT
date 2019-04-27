package com.herolds.discreenkt.service.exception;

import com.herolds.discreenkt.api.listener.events.ErrorEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by herold on 2018. 02. 03..
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DiscreenKTException extends Throwable {

	private ErrorEvent errorEvent;

	public DiscreenKTException(ErrorEvent errorEvent) {
		this.errorEvent = errorEvent;
	}

	public DiscreenKTException(String errorMessage) {
		this.errorEvent = new ErrorEvent(errorMessage);
	}
}
