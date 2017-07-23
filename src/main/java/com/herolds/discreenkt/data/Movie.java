package com.herolds.discreenkt.data;

import java.io.Serializable;

/**
 * Movie entity for kritikustomeg movies.
 * Created by Benedek Herold on 2017.07.16.
 */
public class Movie implements Serializable {

    private int KTid;
    private String title;
    private String secondaryTitle;
    private String link;
    private int year;

    public Movie() {
    }

    public Movie(int KTid, String title, String secondaryTitle, String link, int year) {
        this.KTid = KTid;
        this.title = title;
        this.secondaryTitle = secondaryTitle;
        this.link = link;
        this.year = year;
    }

    public int getKTid() {
        return KTid;
    }

    public void setKTid(int KTid) {
        this.KTid = KTid;
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
