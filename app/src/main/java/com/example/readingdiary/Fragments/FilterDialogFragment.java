package com.example.readingdiary.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.readingdiary.Classes.multispinner;
import com.example.readingdiary.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class FilterDialogFragment extends DialogFragment implements multispinner.multispinnerListener {
    FilterDialogFragment.FilterDialogListener listener;
//    String[] choices;
    List<String> authors = new ArrayList<>();
    List<String> genres = new ArrayList<>();
    private ArrayList<String> uncheckedAuthors = new ArrayList<>();
    private ArrayList<String> uncheckedGenres = new ArrayList<>();

    boolean[] authorsCheck;
    boolean[] genresCheck;
    int position;
    public FilterDialogFragment(TreeSet<String> authors, TreeSet<String> genres,
                                ArrayList<String> uncheckedAuthors,
                                ArrayList<String> uncheckedGenres){
        this.authors = new ArrayList<>(authors);
        this.genres = new ArrayList<>(genres);
        Log.d("qwerty0101", uncheckedAuthors.size() + " " + uncheckedGenres.size());
        this.uncheckedAuthors = uncheckedAuthors;
        this.uncheckedGenres = uncheckedGenres;
        authorsCheck = new boolean[this.authors.size()];
        genresCheck = new boolean[this.genres.size()];
        for (int i = 0; i < authorsCheck.length; i++){
            if (uncheckedAuthors.contains(this.authors.get(i))){
                Log.d("qwerty0101", "uncheckedAuthor");
                authorsCheck[i]=false;
            }
            else{
                authorsCheck[i] = true;
            }

        }
        for (int i = 0; i < genresCheck.length; i++){
            if (uncheckedGenres.contains(this.genres.get(i))){
                genresCheck[i]=false;
            }
            else{
                genresCheck[i] = true;
            }
        }

    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String title = "Фильтрация";
        final String acceptButtonString = getResources().getString(R.string.setCoverDialogAcceptButton);
        final String cancelButtonString = getResources().getString(R.string.setCoverDialogCancelButton);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_dialog, null);
        multispinner genreSpinner = (multispinner) view.findViewById(R.id.genre_spinner);
        genreSpinner.setType(0);
        multispinner authorSpinner = (multispinner) view.findViewById(R.id.author_spinner);
        authorSpinner.setType(1);

        genreSpinner.setItems(genres, genresCheck, "По жанру", this);
        authorSpinner.setItems(authors, authorsCheck, "По автору", this);


//        View view = infla
        builder.setTitle(title);
        builder.setView(view);
////        builder.setMessage(message);
//        Log.d("dialogHate", choices.length+"");
//        builder.setSingleChoiceItems(choices, position, null);
//        Log.d("dialogHate", choices.length+" 2");
//        builder.set
        builder.setPositiveButton(acceptButtonString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), acceptButtonString,
                        Toast.LENGTH_LONG).show();
                acceptChanges();
//                listener.onSortClick(((AlertDialog)dialog).getListView().getCheckedItemPosition());
            }
        });
        builder.setNegativeButton(cancelButtonString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), cancelButtonString,
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.setCancelable(true);
        return builder.create();
    }

    private void acceptChanges(){
        ArrayList<String> uncheckedAuthors = new ArrayList<>();
        ArrayList<String> uncheckedGenres = new ArrayList<>();
        for (int i = 0; i < authorsCheck.length; i++){
            if (authorsCheck[i]==false){
                uncheckedAuthors.add(authors.get(i));
            }
        }
        for (int i = 0; i < genresCheck.length; i++){
            if (genresCheck[i]==false){
                uncheckedGenres.add(genres.get(i));
            }
        }
        listener.onFilterClick(uncheckedAuthors, uncheckedGenres);


    }

    public interface FilterDialogListener{
        void onFilterClick(ArrayList<String> authors, ArrayList<String> genres);
//        void onSortClick(int position);
    }

    @Override
    public void onItemsChecked(boolean[] checked, int checkType) {
        if (checkType==0){
            genresCheck = checked;
        }
        else if (checkType==1){
            authorsCheck = checked;
        }
        String s = checkType + " ";
        for (int i = 0; i < checked.length; i++){
            s += checked[i] + " ";
        }
        Log.d("qwerty0987", s);

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (FilterDialogListener) context;

    }
}
