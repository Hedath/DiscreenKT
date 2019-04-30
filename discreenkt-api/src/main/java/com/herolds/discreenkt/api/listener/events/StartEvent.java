package com.herolds.discreenkt.api.listener.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by herold on 2018. 02. 03..
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class StartEvent extends BaseEvent {

	private int numberOfPages;
}
