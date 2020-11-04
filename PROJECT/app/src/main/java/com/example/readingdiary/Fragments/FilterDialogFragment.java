package com.example.readingdiary.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.readingdiary.Classes.multispinner;
import com.example.readingdiary.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class FilterDialogFragment extends DialogFragment implements multispinner.multispinnerListener {
    FilterDialogFragment.FilterDialogListener listener;
//    String[] choices;
    List<String> authors = new ArrayList<>();
    List<String> genres = new ArrayList<>();
    private ArrayList<String> checkedAuthors = new ArrayList<>();
    private ArrayList<String> checkedGenres = new ArrayList<>();
    private ArrayList<String> genresID;
    private ArrayList<String> sortedGenres;
    private EditText ratingStartView;
    private EditText ratingEndView;
    private MaterialCheckBox materialCheckBox;




    boolean[] authorsCheck;
    boolean[] genresCheck;
    int position;
    boolean showCatalog;
    public FilterDialogFragment(ArrayList<String> authors, ArrayList<String> genres, ArrayList<String> genresID,
                                ArrayList<String> checkedAuthors,
                                ArrayList<String> checkedGenres, boolean showCatalog){
        this.authors = authors;
        this.genres = genres;
        this.genresID = genresID;
        this.checkedAuthors = checkedAuthors;
        this.checkedGenres = checkedGenres;
        this.sortedGenres = new ArrayList<>(genres);
        Collections.sort(this.sortedGenres);
        this.sortedGenres.add(0, "Не указан");
        this.genres.add("Не указан");
        authorsCheck = new boolean[this.authors.size()];
        genresCheck = new boolean[this.genres.size()];
        this.showCatalog = showCatalog;
        for (int i = 0; i < authorsCheck.length; i++){
            if (!checkedAuthors.contains(this.authors.get(i))){
                authorsCheck[i]=false;
            }
            else{
                authorsCheck[i] = true;
            }

        }
        for (int i = 0; i < genresCheck.length; i++){
            if (!checkedGenres.contains(this.sortedGenres.get(i))){
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
        final View view = inflater.inflate(R.layout.filter_dialog, null);

        multispinner genreSpinner = (multispinner) view.findViewById(R.id.genre_spinner);
        genreSpinner.setType(0);
        multispinner authorSpinner = (multispinner) view.findViewById(R.id.author_spinner);
        authorSpinner.setType(1);

        genreSpinner.setItems(sortedGenres, genresCheck, "По жанру", this);
        authorSpinner.setItems(authors, authorsCheck, "По автору", this);


        MaterialTextView ratingView = (MaterialTextView) view.findViewById(R.id.filter_rating_view);
        final LinearLayout ratingLayout = (LinearLayout) view.findViewById(R.id.edit_rating_layout);
        ratingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ratingLayout.getVisibility()==View.VISIBLE){
                    ratingLayout.setVisibility(View.GONE);
                }
                else{
                    ratingLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        ratingStartView = (EditText) view.findViewById(R.id.edit_rating_start);
        ratingEndView = (EditText) view.findViewById(R.id.edit_rating_end);

        materialCheckBox = view.findViewById(R.id.checkbox_show_catalog);
        materialCheckBox.setChecked(showCatalog);


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
        ArrayList<String> checkedAuthors = new ArrayList<>();
        ArrayList<String> checkedGenres = new ArrayList<>();
        for (int i = 0; i < authorsCheck.length; i++){
            if (authorsCheck[i]==true){
                checkedAuthors.add(authors.get(i));
            }
        }
        ArrayList<String> checkedGenresID = new ArrayList<>();
        boolean emptyCheck = false;
        for (int i = 0; i < genresCheck.length; i++){
            if (genresCheck[i]==true){
                if (sortedGenres.get(i).equals("Не указан")){
                    emptyCheck=true;
                    continue;
                }
                checkedGenresID.add(genresID.get(genres.indexOf(sortedGenres.get(i))));
                checkedGenres.add(sortedGenres.get(i));
            }
        }
        if (emptyCheck){
            checkedGenres.add("Не указан");
        }
        listener.onFilterClick(checkedAuthors, checkedGenres, checkedGenresID, ratingStartView.getText().toString(), ratingEndView.getText().toString(), materialCheckBox.isChecked());


    }

    public interface FilterDialogListener{
        void onFilterClick(ArrayList<String> authors, ArrayList<String> genres, ArrayList<String> checkedGenresID, String ratingStart, String ratingEnd, boolean showCatalog);
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
