package com.herolds.discreenkt.api;

import javax.inject.Singleton;

import com.herolds.discreenkt.api.config.ConfigProvider;
import com.herolds.discreenkt.api.data.MovieFactory;
import com.herolds.discreenkt.api.service.DiscreenKTCache;
import com.herolds.discreenkt.api.service.MovieListParser;
import com.herolds.discreenkt.api.service.MoviePosterManager;

import dagger.Module;
import dagger.Provides;

@Module
public class DiscreenKTAPIModule {
	
	@Provides
	@Singleton
	public ConfigProvider provideConfigProvider() {
		return new ConfigProvider();
	}
	
	@Provides
	public DiscreenKTAPI provideDiscreenKTAPI(
			MoviePosterManager moviePosterManager, 
			ConfigProvider configProvider, 
			MovieListParser movieListParser,
			DiscreenKTCache discreenKTCache) {
		return new DiscreenKTAPI(moviePosterManager, configProvider, movieListParser, discreenKTCache);
	}
	
	@Provides
	@Singleton
	public MoviePosterManager provideMoviePosterManager(
			ConfigProvider configProvider, 
			DiscreenKTCache movieCache) {
		return new MoviePosterManager(configProvider, movieCache);
	}
	
	@Provides
	@Singleton
	public MovieFactory provideMovieFactory() {
		return new MovieFactory();
	}
	
	@Provides
	@Singleton
	public MovieListParser provideMovieListParser(MovieFactory movieFactory) {
		return new MovieListParser(movieFactory);
	}
	
	@Provides
	@Singleton
	public DiscreenKTCache provideDiscreenKTCache(ConfigProvider configProvider) {
		return new DiscreenKTCache(configProvider);
	}
}
