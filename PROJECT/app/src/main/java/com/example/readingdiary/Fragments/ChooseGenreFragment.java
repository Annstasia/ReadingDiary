package com.example.readingdiary.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.readingdiary.Activities.EditNoteActivity;
import com.example.readingdiary.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class ChooseGenreFragment extends DialogFragment {
    ArrayList<CheckBox> genres;
    ArrayList<String> allGenres;
    ArrayList<String> allGenresID;
    EditNoteActivity activity;
    LinearLayout linearLayout;
    Button addGenreButton;

    public ChooseGenreFragment(EditNoteActivity activity, ArrayList<String> allGenres, ArrayList<String> allGenresID, ArrayList<String> chosenGenres){
        genres = new ArrayList<>(allGenres.size());
        this.allGenresID = allGenresID;
        this.allGenres = allGenres;
        this.activity=activity;
        ArrayList<String> sortedGenres = new ArrayList<String>(allGenres);
        Collections.sort(sortedGenres);
        for (String i : sortedGenres){
            CheckBox checkBox = new CheckBox(activity.getApplicationContext());
            checkBox.setText(i);
            if (chosenGenres.contains(i)){
                checkBox.setChecked(true);
            }
//            checkBox.setChecked((boolean)chosenGenres.get(i));
            checkBox.setPadding(10, 10, 10, 10);
            checkBox.setTextSize(18);
            genres.add(checkBox);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.choose_genre, null);
        builder.setView(view);
        builder.setNegativeButton("neg", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("pos", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                for (CheckBox checkBox : genres){
                    if (checkBox.isChecked()){
                        hashMap.put(allGenresID.get(allGenres.indexOf(checkBox.getText().toString())), checkBox.getText().toString());
                    }
                }
                activity.changeGenres(hashMap);
            }
        });
        ScrollView scrollView = new ScrollView(getContext());
        linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout);
        for (CheckBox i : genres){
            linearLayout.addView(i);
        }

        addGenreButton = new Button(getContext());
        addGenreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGenreButtonPressed();
            }
        });
        addGenreButton.setText("text");
        linearLayout.addView(addGenreButton);
        builder.setView(scrollView);
        return builder.create();
    }

    public void addGenreButtonPressed(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        final EditText editText = new EditText(getContext());
        builder.setView(editText);
        builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addGenre(editText.getText().toString().trim());
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                return null;
            }
        });
        builder.create().show();
    }

    public void addGenre(String newGenre){
        if (newGenre == ""){
            Toast.makeText(getContext(), "Введена пустая строка", Toast.LENGTH_LONG).show();
            return;
        }
        CheckBox checkBox = new CheckBox(activity.getApplicationContext());
        checkBox.setText(newGenre);
        checkBox.setChecked(true);
        checkBox.setPadding(10, 10, 10, 10);
        checkBox.setTextSize(18);
        genres.add(checkBox);
        linearLayout.addView(checkBox, genres.size()-1);
        long id = System.currentTimeMillis();
        allGenres.add(newGenre);
        allGenresID.add(id+"");
        activity.addGenre(id, newGenre);
    }

}
