package com.herolds.discreenkt.data;

/**
 * @author bence
 */
public class MovieFactory {

    // TODO: proper logging

    public static Movie create(String title, String secondaryTitle, String link) {
        int id = getKTid(link);
        int year = getYear(link);

        return new Movie(id, title, secondaryTitle, link, year);
    }

    private static int getKTid(String movieLink) {
        movieLink = movieLink.replace("/film/", "");

        int idEndIndex = movieLink.indexOf('/');

        if (idEndIndex == -1) {
            System.out.println("Could not find id: " + movieLink);
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
            System.out.println("Could not find year: " + movieLink);

            return 0;
        }

        String year = movieLink.substring(yearStartIndex + 1);

        try {
            return Integer.parseInt(year);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
