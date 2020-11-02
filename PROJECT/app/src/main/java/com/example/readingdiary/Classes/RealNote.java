package com.example.readingdiary.Classes;

import android.net.Uri;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

// класс для записей
public class RealNote implements Note {
    private String path;
    private String author;
    private String title;
    private String id;
    private final int type = 0;
    private double rating;
    private boolean isPrivate;
    private Uri coverUri;
    private double publicRatingSum;
    private long publicRatingCount;
    private String owner;
    private long time;
    private HashMap<String, Object> genre;
    private boolean visibility=true;





    public RealNote(String id, String path, String author, String title, double rating, boolean isPrivate, double publicRatingSum, long publicRatingCount, Uri coverUri, HashMap<String, Object> genre){
        this.id = id;
        this.path = path;
        this.title = title;
        this.author = author;
        this.rating = rating;
        this.coverUri = coverUri;
        this.isPrivate = isPrivate;
        this.publicRatingSum=publicRatingSum;
        this.publicRatingCount=publicRatingCount;
        this.genre = genre;
    }
    public RealNote(String id, String path, String author, String title, double rating, boolean isPrivate, double publicRatingSum, long publicRatingCount, HashMap<String, Object> genre){
        this.id = id;
        this.path = path;
        this.title = title;
        this.author = author;
        this.rating = rating;
        this.isPrivate = isPrivate;
        this.publicRatingSum=publicRatingSum;
        this.publicRatingCount=publicRatingCount;
        this.genre = genre;

    }


    public String getPath() {
        return path;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean changePrivate(){
        isPrivate = !isPrivate;
        return isPrivate;
    }

    public double getPublicRatingSum() {
        return publicRatingSum;
    }

    public void setPublicRatingSum(double publicRatingSum) {
        this.publicRatingSum = publicRatingSum;
    }

    public long getPublicRatingCount() {
        return publicRatingCount;
    }

    public void setPublicRatingCount(long publicRatingCount) {
        this.publicRatingCount = publicRatingCount;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Uri getCoverUri() {
        return coverUri;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setCoverPath(Uri coverUri) {
        this.coverUri = coverUri;
    }

    public HashMap<String, Object> getGenre() {
        return genre;
    }

    public void setGenre(HashMap<String, Object> genre) {
        this.genre = genre;
    }

    @Override
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public boolean getVisibility() {
        return visibility;
    }

    @Override
    public int getItemType() {
        return type;
    }

    @Override
    public String getID() {
        return id;
    }


}

