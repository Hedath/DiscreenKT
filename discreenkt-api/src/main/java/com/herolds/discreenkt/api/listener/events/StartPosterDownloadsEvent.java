package com.herolds.discreenkt.api.listener.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class StartPosterDownloadsEvent extends BaseEvent {
	private int numberOfMovies;
}
