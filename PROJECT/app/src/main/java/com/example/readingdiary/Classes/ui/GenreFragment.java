package com.example.readingdiary.Classes.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.readingdiary.R;

public class GenreFragment extends Fragment {

    private GenreViewModel genreViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        genreViewModel =
                ViewModelProviders.of(this).get(GenreViewModel.class);
        View root = inflater.inflate(R.layout.fragment_genres, container, false);
        final TextView textView = root.findViewById(R.id.text_genres);
        genreViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        Log.d("Qwerty12321", "genre");
        return root;
    }
}
