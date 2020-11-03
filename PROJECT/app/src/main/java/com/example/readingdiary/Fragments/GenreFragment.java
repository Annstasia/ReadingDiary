package com.example.readingdiary.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.ui.GenreViewModel;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.GenreAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GenreFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String user;
    private GenreViewModel genreViewModel;
    private ArrayList<String> sortedGenres;
    private RecyclerView recyclerView;
    private View root;
    private GenreAdapter adapter;
    private ArrayList<String> genres;
    private ArrayList<String> genresID;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_genres, null);
        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("genres").document(user).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.getData() != null){
                    genres = new ArrayList<>();
                    for (Object ob : documentSnapshot.getData().values()){
                        genres.add(ob.toString());
                    }
                    genresID = new ArrayList<>(documentSnapshot.getData().keySet());
                    sortedGenres = new ArrayList<>(genres);
                    Collections.sort(sortedGenres);
                    setAdapter();

                }
            }
        });
        ((TextView)getActivity().findViewById(R.id.counter_text)).setText("Жанры");
        ((Toolbar)getActivity().findViewById(R.id.toolbar_navigation)).getMenu().clear();
        FloatingActionButton addGenreButton = root.findViewById(R.id.add_genre_button);
        addGenreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                builder.setCancelable(false);
                builder.create().show();
            }
        });

        return root;
    }

    private void addGenre(String newGenre){
        Map<String, Object> map = new HashMap<>();
        long id = System.currentTimeMillis();
        map.put(id+"", newGenre);
        db.collection("genres").document(user).set(map, SetOptions.merge());
        int index = genres.size();
        for (int i = 0; i < sortedGenres.size(); i++){
            if (sortedGenres.get(i).compareTo(newGenre)>0){
                index=i;
                break;
            }
        }
        sortedGenres.add(index, newGenre);
        adapter.notifyItemInserted(index);
        genres.add(newGenre);
        genresID.add(id+"");
    }

    private void setAdapter(){
        recyclerView = root.findViewById(R.id.genre_recycler);
        adapter = new GenreAdapter(sortedGenres);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
        adapter.setOnGenreAdapterListener(new GenreAdapter.GenreAdapterListener() {
            @Override
            public void onGenreChanged(String genre, int position) {
                if (!genres.get(position).equals(genre)){
                    db.collection("genres").document(user).update(genresID.get(genres.indexOf(sortedGenres.get(position))), genre.trim());
//                    db.collection("genres").document(user).update(genres.get(position), FieldValue.delete());
//                    Map<String, Object> map= new HashMap<>();
//                    map.put(genre.trim(), false);
//                    db.collection("genres").document(user).set(map, SetOptions.merge());
                    genres.set(position, genre.trim());
                    sortedGenres.set(position, genre);
                    adapter.notifyItemChanged(position);
                }

            }

            @Override
            public void onDelete(int position) {
                db.collection("genres").document(user).update(genresID.get(genres.indexOf(sortedGenres.get(position))), FieldValue.delete());
                genres.remove(position);
                adapter.notifyItemRemoved(position);
            }
        });

    }

}
