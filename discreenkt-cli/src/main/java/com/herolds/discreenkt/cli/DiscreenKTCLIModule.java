package com.herolds.discreenkt.cli;

import javax.inject.Singleton;

import com.herolds.discreenkt.api.DiscreenKTAPI;
import com.herolds.discreenkt.api.config.ConfigProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class DiscreenKTCLIModule {

	@Provides
	@Singleton
	public DiscreenKTCLI provideApplication(ConfigProvider configProvider, DiscreenKTAPI discreenKTAPI) {
		return new DiscreenKTCLI(configProvider, discreenKTAPI);
	}
}
