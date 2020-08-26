package com.example.readingdiary.Classes;

import android.net.Uri;

public class VariousNotesAudio implements VariousNotesInterface, Comparable<VariousNotesInterface>{
    int itemType=1;
    long time;
    Uri uri;
    boolean playing = false;

    public VariousNotesAudio(long time, Uri uri){
        this.time = time;
        this.uri = uri;
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
}
