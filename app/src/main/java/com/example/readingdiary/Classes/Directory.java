package com.example.readingdiary.Classes;


// класс для директорий
public class Directory implements Note {
    private String id;
    private String directory;
    private final int type = 1;
    private boolean visibility = true;
    public Directory(String id, String directory){
        this.id = id;
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    @Override
    public int getItemType() {
        return type;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public boolean getVisibility() {
        return visibility;
    }
}
