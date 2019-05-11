package com.herolds.discreenkt.cli;

import javax.inject.Singleton;

import com.herolds.discreenkt.api.DiscreenKTAPIModule;

import dagger.Component;

@Singleton
@Component(modules = { DiscreenKTAPIModule.class, DiscreenKTCLIModule.class })
public interface DiscreenKTCLIComponent {
	DiscreenKTCLI getDiscreenKTCLI();
}
