package com.herolds.discreenkt.api.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bence
 */
public class MovieFactory {

	private static final Logger logger = LoggerFactory.getLogger(MovieFactory.class);

	public static Movie create(String title, String secondaryTitle, String link) {
		return Movie.builder()
				.KTid(getKTid(link))
				.year(getYear(link))
				.title(title)
				.secondaryTitle(secondaryTitle)
				.link(link)
				.build();
	}

	private static int getKTid(String movieLink) {
		movieLink = movieLink.replace("/film/", "");

		int idEndIndex = movieLink.indexOf('/');

		if (idEndIndex == -1) {
			logger.error("Could not find id in movie link: {}", movieLink);
			return 0;
		}

		String id = movieLink.substring(0, idEndIndex);

		try {
			return Integer.parseInt(id);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private static int getYear(String movieLink) {
		int yearStartIndex = movieLink.lastIndexOf('-');

		if (yearStartIndex == -1) {
			logger.error("Could not find year: ", movieLink);
			return 0;
		}

		String year = movieLink.substring(yearStartIndex + 1);

		try {
			return Integer.parseInt(year);
		} catch (NumberFormatException e) {
			logger.error("Error while parsing year in movie link: {}", movieLink);
			logger.error("Exception: ", e);
			
			return 0;
		}
	}
}
