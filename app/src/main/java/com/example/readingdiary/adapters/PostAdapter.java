package com.example.readingdiary.adapters;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.Note;
import com.example.readingdiary.Classes.RealNote;
import com.example.readingdiary.R;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostPosts>
{
    public interface OnItemClickListener{
        void onItemClick(int position);
        void onRatingChanged(int position, float rating);
    }

    public PostAdapter.OnItemClickListener listener;
    private List<RealNote> notes;

    public PostAdapter(List<RealNote> notes)
    {
        this.notes = notes;
    }

    public void setOnItemClickListener(PostAdapter.OnItemClickListener listener){
        this.listener = listener;    }

    @Override
    public PostPosts onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_activity, viewGroup, false);
        PostAdapter.PostPosts vh = new PostAdapter.PostPosts(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull PostPosts holder, int position) {
        RealNote realNote = (RealNote) notes.get(position);
        holder.tvTextPost.setText(realNote.getAuthor());
        holder.tvNamePost.setText(realNote.getTitle());
        holder.ratingBar2.setRating((float)realNote.getRating());
        Log.d("qwerty53", position + " " +realNote.getID());
        if (realNote.getCoverUri() !=null){
            holder.imageView3.setVisibility(View.VISIBLE);
            Log.d("qwerty53", position + " " +realNote.getID() + " " + realNote.getCoverUri().toString());
            Picasso.get().load(realNote.getCoverUri()).into(holder.imageView3);
        }
        else{
            holder.imageView3.setVisibility(View.INVISIBLE);
//            Picasso.get().load(realNote.getCoverUri()).into(holder.imageView3);
        }
        if (realNote.getPublicRatingCount() != 0)
            holder.tvReitPost.setText(new DecimalFormat("#0.00").format(realNote.getPublicRatingSum() / realNote.getPublicRatingCount()));
        else{
            holder.tvReitPost.setText("0.0");
        }
//        holder.tvNamePost.setText("Asdfghj");
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }



    class PostPosts extends RecyclerView.ViewHolder {
        private TextView tvNamePost;
        private TextView tvTextPost;
        private TextView tvReitPost;
        private ImageView imageView3;
        private RatingBar ratingBar2;

        PostPosts(final View itemView) {
            super(itemView);
            tvNamePost = (TextView) itemView.findViewById(R.id.tvNamePost);
            tvTextPost = (TextView) itemView.findViewById(R.id.tvTextPost);
            tvReitPost = (TextView) itemView.findViewById(R.id.tvReitPost);
            imageView3 = (ImageView) itemView.findViewById(R.id.imageView3);
            ratingBar2 = (RatingBar) itemView.findViewById(R.id.ratingBar2);

            ratingBar2.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(final RatingBar ratingBar, float rating, boolean fromUser) {
                    if (fromUser){
                        final Handler uiHandler = new Handler();

                        final Runnable makeLayoutGone = new Runnable(){
                            @Override
                            public void run(){
                                ratingBar2.setClickable(true);
                            }
                        };
                        ratingBar2.setClickable(false);
                        uiHandler.postDelayed(makeLayoutGone, 500);


                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            ratingBar2.setClickable(false);
                            listener.onRatingChanged(position, rating);
                        }
                    }

                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });



        }

    }

}
