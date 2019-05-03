package com.herolds.discreenkt.gui.scheduler;

import java.util.Optional;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.config.ConfigProvider;
import com.herolds.discreenkt.gui.enums.SynchronizationInterval;
import com.herolds.discreenkt.gui.scheduler.job.DownloadPostersJob;

public class PosterDownloadScheduler {

	private final Logger logger = LoggerFactory.getLogger(PosterDownloadScheduler.class);
	
	private static PosterDownloadScheduler instance;

	private Scheduler scheduler;
	
	private ConfigProvider configProvider;
	
	private PosterDownloadScheduler() throws SchedulerException {
		this.configProvider = ConfigProvider.getInstance();
		this.scheduler = StdSchedulerFactory.getDefaultScheduler();
		
		JobDetail job = JobBuilder.newJob(DownloadPostersJob.class)
        	    .withIdentity("downloadPostersJob")
        	    .storeDurably()
        	    .build();
		
    	scheduler.addJob(job, false);    	
    	scheduler.start();
    	
    	logger.info("Started scheduler. Registered job: {}", DownloadPostersJob.class.getName());
    	
    	schedule();
	}
	
	public void schedule() {
		Optional<SynchronizationInterval> syncInterval = SynchronizationInterval.getEnumValue(configProvider.getSyncInterval());

		if (syncInterval.isPresent() && !SynchronizationInterval.NONE.equals(syncInterval.get())) {
			// Define a Trigger that will fire "now", and not repeat
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity("downloadPostersTrigger")
					.forJob("downloadPostersJob")
					.startNow()
					.withSchedule(createCronScheduleBuilder(syncInterval.get(), configProvider.getSyncTime()))
					.build();
			
			try {
				logger.info("Scheduling job: {} Interval: {} Time: {}", DownloadPostersJob.class.getName(), syncInterval.get(), configProvider.getSyncTime());
				scheduler.scheduleJob(trigger);
				logger.info("Scheduled job: {} Next fire: {}", DownloadPostersJob.class.getName(), trigger.getNextFireTime());
			} catch (SchedulerException e) {
				logger.error("Error occured during schedule: ", e);
			}
		}
	}
	
	public void reschedule() throws SchedulerException {
		Optional<SynchronizationInterval> syncInterval = SynchronizationInterval.getEnumValue(configProvider.getSyncInterval());

		TriggerKey oldTriggerKey = TriggerKey.triggerKey("downloadPostersTrigger");
		Trigger oldTrigger = scheduler.getTrigger(oldTriggerKey);
		
		if (syncInterval.isPresent() && !SynchronizationInterval.NONE.equals(syncInterval.get())) {
			
			Trigger trigger = createTrigger(syncInterval.get());
			if (oldTrigger != null) {
				logger.info("Rescheduling job: {} Interval: {} Time: {}", DownloadPostersJob.class.getName(), syncInterval.get(), configProvider.getSyncTime());
				scheduler.rescheduleJob(oldTrigger.getKey(), trigger);				
				logger.info("Rescheduled job: {} Next fire: {}", DownloadPostersJob.class.getName(), trigger.getNextFireTime());
			} else {
				logger.info("Scheduling job (no previous trigger): {}. Interval: {} Time: {}", DownloadPostersJob.class.getName(), syncInterval.get(), configProvider.getSyncTime());
				scheduler.scheduleJob(trigger);
				logger.info("Scheduled job: {} Next fire: {}", DownloadPostersJob.class.getName(), trigger.getNextFireTime());
			}
		} else {
			if (oldTrigger != null) {
				scheduler.unscheduleJob(oldTriggerKey);
				logger.info("Unscheduled job: {}");
			}
		}
	}
	
	public static PosterDownloadScheduler getInstance() throws SchedulerException {
		if (instance == null) {
			instance = new PosterDownloadScheduler();
		}
		
		return instance;
	}
	
	public void close() throws SchedulerException {
		scheduler.shutdown();
	}
	
	private Trigger createTrigger(SynchronizationInterval syncInterval) {
		return TriggerBuilder.newTrigger()
				.withIdentity("downloadPostersTrigger")
				.forJob("downloadPostersJob")
				.startNow()
				.withSchedule(createCronScheduleBuilder(syncInterval, configProvider.getSyncTime()))
				.build();
	}
	
	private CronScheduleBuilder createCronScheduleBuilder(SynchronizationInterval synchronizationInterval, String syncTime) {
		String[] syncTimeSplitted = syncTime.split(":");
		int hour = Integer.parseInt(syncTimeSplitted[0]);
		int minutes = Integer.parseInt(syncTimeSplitted[1]);

		switch (synchronizationInterval) {
		case DAILY:
			return CronScheduleBuilder.dailyAtHourAndMinute(hour, minutes);
		case MONTHLY_FIRST:
			return CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, hour, minutes);
		case MONTHLY_LAST:
			return CronScheduleBuilder.cronSchedule(String.format("0 %d %d L * ?", minutes, hour));
		case WEEKLY_MONDAY:
			return CronScheduleBuilder.weeklyOnDayAndHourAndMinute(DateBuilder.MONDAY, hour, minutes);
		case WEEKLY_SUNDAY:
			return CronScheduleBuilder.weeklyOnDayAndHourAndMinute(DateBuilder.SUNDAY, hour, minutes);
		default:
			return null;
		}
	}
}
