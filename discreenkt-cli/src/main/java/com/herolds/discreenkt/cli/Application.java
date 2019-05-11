package com.herolds.discreenkt.cli;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application.
 * Created by Benedek Herold on 2017.07.15.
 */
public class Application {
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
    public static void main(String[] args) {
        DiscreenKTCLIComponent discreenKTCLIComponent = DaggerDiscreenKTCLIComponent.builder().build();
    	
        DiscreenKTCLI discreenKTCLI = discreenKTCLIComponent.getDiscreenKTCLI();
        
    	try {
    		discreenKTCLI.parseArguments(args);
        } catch (IOException | ParseException e) {
        	logger.error("Error while parsing arguments: ", e);
            return;
        }

    	discreenKTCLI.downloadPosters();
    }
}
