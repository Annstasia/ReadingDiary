package com.example.readingdiary.Classes;

public class VariousNotes implements VariousNotesInterface, Comparable<VariousNotesInterface> {
    private String text;
    private String path;
    private long time;
    private boolean changed;
    private boolean needsUpdate;
    int itemType = 0;
    public VariousNotes(String text, String path, long time, boolean changed, boolean needsUpdate){
        this.text = text;
        this.path = path;
        this.changed = changed;
        this.time = time;
        this.needsUpdate = needsUpdate;
    }

    public boolean isNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    @Override
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    @Override
    public int compareTo(VariousNotesInterface o) {
        return (int)(o.getTime() - this.time);
    }
}
