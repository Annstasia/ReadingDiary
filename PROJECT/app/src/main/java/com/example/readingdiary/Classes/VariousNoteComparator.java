package com.example.readingdiary.Classes;


import java.util.Comparator;

public class VariousNoteComparator implements Comparator<VariousNotesInterface> {
    @Override
    public int compare(VariousNotesInterface o1, VariousNotesInterface o2) {
        if (o1.getTime() - o2.getTime() < 0){
            return -1;
        }
        else if (o1.getTime() == o2.getTime()){
            return 0;
        }
        else{
            return 1;
        }
    }
}
