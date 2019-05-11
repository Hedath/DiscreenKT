package com.herolds.discreenkt.gui.scheduler.job;

import java.net.URISyntaxException;

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.DiscreenKTAPI;
import com.herolds.discreenkt.api.listener.DefaultListener;
import com.herolds.discreenkt.api.listener.DiscreenKTListener;
import com.herolds.discreenkt.gui.Main;

public class DownloadPostersJob extends DefaultListener implements Job, DiscreenKTListener {

	private final Logger logger = LoggerFactory.getLogger(DownloadPostersJob.class);

	@Inject
	protected DiscreenKTAPI discreenKTAPI;

	public DownloadPostersJob() throws URISyntaxException {
		Main.injector.inject(this);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			DiscreenKTListener listener = (DiscreenKTListener) context.getScheduler().getContext().get("listener");
			discreenKTAPI.setListener(listener);
		} catch (SchedulerException e) {
			logger.error("Could not register listener for job: ", e);
		}
		
		discreenKTAPI.startDownload();
	}

}
