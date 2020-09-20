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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SubscriptionsShowAdapter extends RecyclerView.Adapter<SubscriptionsShowAdapter.ViewHolder>
{
    public interface OnItemClickListener{
        void onRemoveSubscription(int position);
//        void onItemClick(int position);
    }

    public SubscriptionsShowAdapter.OnItemClickListener listener;
    private List<String> subscriptions;

    public SubscriptionsShowAdapter(List<String> subscriptions)
    {
        this.subscriptions = subscriptions;
    }

    public void setOnItemClickListener(SubscriptionsShowAdapter.OnItemClickListener listener){
        this.listener = listener;    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subscriptions_id_item, viewGroup, false);
        SubscriptionsShowAdapter.ViewHolder vh = new SubscriptionsShowAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.subscriptionTextView.setText("ID: " + subscriptions.get(position));
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        private MaterialTextView subscriptionTextView;
        private MaterialButton removeSubscriptionButton;

        ViewHolder(final View itemView) {
            super(itemView);
            subscriptionTextView = (MaterialTextView) itemView.findViewById(R.id.subscriptions_id_text);
            removeSubscriptionButton = (MaterialButton) itemView.findViewById(R.id.remove_subscription_button);
            removeSubscriptionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onRemoveSubscription(position);
                        }

                    }
                }
            });



        }

    }

}
