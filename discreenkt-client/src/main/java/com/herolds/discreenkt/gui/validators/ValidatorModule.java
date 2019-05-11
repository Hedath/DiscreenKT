package com.herolds.discreenkt.gui.validators;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ValidatorModule {

	@Provides
	@Singleton
	public PathValidator providePathValidator() {
		return new PathValidator();
	}
	
	@Provides
	@Singleton
	public TimeValidator provideTimeValidator() {
		return new TimeValidator();
	}
	
	@Provides
	@Singleton
	public UserURLValidator provideUserURLValidator() {
		return new UserURLValidator();
	}
}
