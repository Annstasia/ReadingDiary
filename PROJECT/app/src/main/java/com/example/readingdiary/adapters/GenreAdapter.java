package com.example.readingdiary.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> {
    private ArrayList<String> genres;

    public interface GenreAdapterListener{
        public void onGenreChanged(String genre, int position);
        public void onDelete(int position);
    }
    GenreAdapterListener genreAdapterListener;
    public GenreAdapter(ArrayList<String> genres){
        this.genres = genres;
    }

    public void setOnGenreAdapterListener(GenreAdapterListener listener){
        genreAdapterListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre_recycler, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.genreView.setText(genres.get(position));
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextInputEditText genreView;
        ImageView editButton;
        ImageView deleteButton;
        public ViewHolder(final View itemView){
            super(itemView);
            genreView=itemView.findViewById(R.id.genre_view);
            editButton = itemView.findViewById(R.id.edit_genre);
            deleteButton = itemView.findViewById(R.id.delete_genre);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!genreView.isEnabled()){
                        editButton.setImageResource(R.drawable.ic_check_dark);
                        genreView.setEnabled(true);
                    }
                    else{
                        if (genreAdapterListener != null){
                            genreAdapterListener.onGenreChanged(genreView.getText().toString(), getAdapterPosition());
                        }
                        editButton.setImageResource(R.drawable.ic_edit_dark);
                        genreView.setEnabled(false);
                    }
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    genreAdapterListener.onDelete(getAdapterPosition());
                }
            });
        }


    }
}
