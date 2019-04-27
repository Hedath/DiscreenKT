package com.herolds.discreenkt.api.listener.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by herold on 2018. 02. 03..
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PosterDownloadEvent extends BaseEvent {

	private String movieTitle;

	private boolean success;
}
