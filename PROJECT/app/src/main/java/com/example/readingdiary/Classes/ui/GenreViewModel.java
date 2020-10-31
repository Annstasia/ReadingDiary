package com.example.readingdiary.Classes.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GenreViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GenreViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is genre fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}