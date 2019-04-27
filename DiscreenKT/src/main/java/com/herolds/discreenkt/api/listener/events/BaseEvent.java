package com.herolds.discreenkt.api.listener.events;

import lombok.Data;

/**
 * Created by herold on 2018. 02. 03..
 */
@Data
public abstract class BaseEvent {

	private String message;

	protected BaseEvent() {

	}

	protected BaseEvent(String message) {
		this.message = message;
	}
}
