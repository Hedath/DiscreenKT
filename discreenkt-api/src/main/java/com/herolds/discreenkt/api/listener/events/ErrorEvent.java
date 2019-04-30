package com.herolds.discreenkt.api.listener.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by herold on 2018. 02. 03..
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ErrorEvent extends BaseEvent {

	public ErrorEvent(String message) {
		super(message);
	}
}
