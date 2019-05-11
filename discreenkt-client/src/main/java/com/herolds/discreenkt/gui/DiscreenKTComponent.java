package com.herolds.discreenkt.gui;

import javax.inject.Singleton;

import com.herolds.discreenkt.api.DiscreenKTAPIModule;
import com.herolds.discreenkt.gui.controller.Controller;
import com.herolds.discreenkt.gui.scheduler.job.DownloadPostersJob;
import com.herolds.discreenkt.gui.validators.ValidatorModule;

import dagger.Component;

@Singleton
@Component(modules = { DiscreenKTAPIModule.class, DiscreenKTModule.class, ValidatorModule.class })
public interface DiscreenKTComponent {
	
	void inject(Main main);
	
	void inject(Controller controller);

	void inject(DownloadPostersJob downloadPostersJob);
}
