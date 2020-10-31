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
import java.util.TreeMap;

public class ChooseGenreFragment extends DialogFragment {
    ArrayList<CheckBox> genres;
    TreeMap<String, Object> chosenGenres;
    EditNoteActivity activity;
    LinearLayout linearLayout;
    Button addGenreButton;

    public ChooseGenreFragment(EditNoteActivity activity, TreeMap<String, Object> chosenGenres){
        genres = new ArrayList<>(chosenGenres.size());
        this.chosenGenres = chosenGenres;
        this.activity = activity;
        for (String i : chosenGenres.keySet()){
            CheckBox checkBox = new CheckBox(activity.getApplicationContext());
            checkBox.setText(i);
            checkBox.setChecked((boolean)chosenGenres.get(i));
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
                for (CheckBox checkBox : genres){
                    chosenGenres.put(checkBox.getText().toString(), checkBox.isChecked());
                }
                activity.changeGenres(chosenGenres);
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
        chosenGenres.put(newGenre, true);
        linearLayout.addView(checkBox, genres.size()-1);
        activity.addGenre(newGenre);
    }

}
