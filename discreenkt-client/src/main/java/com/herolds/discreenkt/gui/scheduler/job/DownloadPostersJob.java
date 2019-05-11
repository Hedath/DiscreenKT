package com.herolds.discreenkt.gui.scheduler.job;

import java.net.URISyntaxException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.DiscreenKTAPI;
import com.herolds.discreenkt.api.config.ConfigProvider;
import com.herolds.discreenkt.api.listener.DefaultListener;
import com.herolds.discreenkt.api.listener.DiscreenKTListener;

public class DownloadPostersJob extends DefaultListener implements Job, DiscreenKTListener {

	private final Logger logger = LoggerFactory.getLogger(DownloadPostersJob.class);

	private DiscreenKTAPI discreenKTAPI;

	public DownloadPostersJob() throws URISyntaxException {
		ConfigProvider.initConfigProvider();

		this.discreenKTAPI = new DiscreenKTAPI(this, null);
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
