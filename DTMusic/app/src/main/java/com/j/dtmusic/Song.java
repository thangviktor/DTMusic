package com.j.dtmusic;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class Song implements Serializable {
    private String title;
    private int id;
    private int duration;
    private String path;
    private int size;
    private String artist;
    private String album;
//    private String thumbnail;
//    private Bitmap bmthumb;

    public Song() {
    }

    public Song(int id, String title, String artist, String album, String path, int duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;

    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

//    public String getThumbnail() {
//        return thumbnail;
//    }
//
//    public void setThumbnail(String thumbnail) {
//        this.thumbnail = thumbnail;
//    }

//    public Bitmap getBmthumb() {
//        return bmthumb;
//    }
//
//    public void setBmthumb(Bitmap bmthumb) {
//        this.bmthumb = bmthumb;
//    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public int getSize() {
//        return size;
//    }
//
//    public void setSize(int size) {
//        this.size = size;
//    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
