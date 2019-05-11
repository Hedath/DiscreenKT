package com.herolds.discreenkt.gui;

import javax.inject.Singleton;

import com.herolds.discreenkt.api.config.ConfigProvider;
import com.herolds.discreenkt.gui.config.FxHelper;
import com.herolds.discreenkt.gui.scheduler.DownloadPostersScheduler;

import dagger.Module;
import dagger.Provides;

@Module
public class DiscreenKTModule {
	
	@Provides
	@Singleton
	public DownloadPostersScheduler provideDownloadPostersScheduler(ConfigProvider configProvider) {
		return new DownloadPostersScheduler(configProvider);
	}
	
	@Provides
	@Singleton
	public FxHelper provideFxHelper() {
		return new FxHelper();
	}
}
