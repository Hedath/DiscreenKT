package com.herolds.mpd.data;

/**
 * Created by Benedek Herold on 2017.07.16.
 */
public class Movie {

    private String title;
    private String secondaryTitle;
    private String link;

    public Movie() {
    }

    public Movie(String title, String secondaryTitle, String link) {
        this.title = title;
        this.secondaryTitle = secondaryTitle;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSecondaryTitle() {
        return secondaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle) {
        this.secondaryTitle = secondaryTitle;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
