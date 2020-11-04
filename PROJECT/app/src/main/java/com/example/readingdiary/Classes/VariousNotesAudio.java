package com.example.readingdiary.Classes;

import android.net.Uri;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class VariousNotesAudio implements VariousNotesInterface, Comparable<VariousNotesInterface>{
    int itemType=1;
    long time;
    Uri uri;
    boolean playing = false;
    String date;

    public VariousNotesAudio(long time, Uri uri){
        this.time = time;
        this.uri = uri;
        GregorianCalendar calendarDate = new GregorianCalendar();
//        calendarDate.setTimeZone(TimeZone.getTimeZone("GMT+4"));
        calendarDate.setTimeInMillis(time);
//        calendarDate.setTimeZone(new );
        calendarDate.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        date = calendarDate.get(Calendar.DATE)+"."+calendarDate.get(Calendar.MONTH)+"."+calendarDate.get(Calendar.YEAR);

    }
    @Override
    public int getItemType() {
        return itemType;
    }

    @Override
    public long getTime() {
        return time;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isPlaying(){
        return playing;
    }

    public void changePlaying(){
        this.playing = !playing;
    }

    public void setPlaying(boolean playing){
        this.playing = playing;
    }

    @Override
    public int compareTo(VariousNotesInterface o) {
        return (int)(o.getTime() - this.time);
    }

    public String getDate() {
        return date;
    }
}
