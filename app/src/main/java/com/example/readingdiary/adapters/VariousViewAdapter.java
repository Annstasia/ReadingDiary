package com.example.readingdiary.adapters;



//package com.example.readingdiary;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.VariousNotes;
import com.example.readingdiary.Classes.VariousNotesAudio;
import com.example.readingdiary.Classes.VariousNotesInterface;
import com.example.readingdiary.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;


public class VariousViewAdapter extends RecyclerView.Adapter<VariousViewAdapter.ViewHolder>{

    private List<VariousNotesInterface> buttons;
    private boolean actionMode;
    private VariousViewAdapter.OnItemClickListener mListener;
    private final int TYPE_TEXT_ITEM = 0;
    private final int TYPE_AUDIO_ITEM = 1;
    public interface OnItemClickListener{
        void onItemClick(int position);
        void onItemLongClick(int position);
        void onCheckClick(int position);
        void onUncheckClick(int position);
        void onPlayButtonPressed(int position, View itemView);
//        void onPlayerSeekBarTouched(int position, View v);

    }



    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public VariousViewAdapter(List<VariousNotesInterface> buttons) {
        this.buttons = buttons; this.actionMode = false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if (viewType == TYPE_TEXT_ITEM){
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.various_view_item, viewGroup, false);
        }
        else{
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.various_audio_view_item, viewGroup, false);
        }
        ViewHolder vh = new ViewHolder(v);
//        v.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        if (!actionMode){
            viewHolder.checkBox.setChecked(false);
            viewHolder.checkBox.setVisibility(View.GONE);
        }
        else{
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        }
        if (buttons.get(i).getItemType()==0)
        {
            viewHolder.textView.setText(((VariousNotes)buttons.get(i)).getText());
        }
        else{
            if (((VariousNotesAudio)buttons.get(i)).isPlaying()){
                viewHolder.playerSeekBar.setEnabled(true);
                viewHolder.playAudioButton.setImageResource(R.drawable.ic_action_pause_light);
            }
            else{
                viewHolder.playerSeekBar.setEnabled(false);
                viewHolder.playAudioButton.setImageResource(R.drawable.ic_action_play_light);
            }
            viewHolder.textView.setText(((VariousNotesAudio)buttons.get(i)).getDate());
        }

    }

    @Override
    public int getItemCount() {
        return buttons.size();
    }

    @Override
    public int getItemViewType(int position) {
        // определяем какой тип в текущей позиции
        int type = buttons.get(position).getItemType();
        if (type == 0) return TYPE_TEXT_ITEM;
        else return TYPE_AUDIO_ITEM;

    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setActionMode(boolean mode){
        actionMode = mode;
    }


    public void clearAdapter() {
        buttons.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private CardView cardView;
        private CheckBox checkBox;
        private FloatingActionButton playAudioButton;
        private SeekBar playerSeekBar;
        ViewHolder(final View itemView) {
            super(itemView);
            Log.d("qwerty55", "newViewHolder");
            textView = (TextView) itemView.findViewById(R.id.variousTextView);
            cardView = (CardView) itemView.findViewById(R.id.variousCardView);
            checkBox = (CheckBox) itemView.findViewById(R.id.variousCheckBox);
            playAudioButton = (FloatingActionButton) itemView.findViewById(R.id.playAudioButton);
            playerSeekBar = (SeekBar) itemView.findViewById(R.id.musicSeekBar);


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            mListener.onItemLongClick(position);
                        }
                        return true;
                    }
                    return false;
                }
            });


//
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()){
                        mListener.onCheckClick(getAdapterPosition());
                    }
                    else{
                        mListener.onUncheckClick(getAdapterPosition());
                    }
                }

            });
            if (playAudioButton != null){
                playAudioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null){
                            mListener.onPlayButtonPressed(getAdapterPosition(), itemView);

//                            playAudioButton.setImageResource(R.drawable.ic_action_pause_light);
                        }
                    }
                });
            }

//            if (playerSeekBar != null){
//                playerSeekBar.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        if (mListener != null){
//                            if (buttons.get(getAdapterPosition()).getItemType() == 1){
//                                if (((VariousNotesAudio) buttons.get(getAdapterPosition())).isPlaying()){
//                                    mListener.onPlayerSeekBarTouched(getAdapterPosition(), v);
//                                }
//                            }
////                            mListener.onPlayerSeekBarTouched
//                        }
//                        return false;
//                    }
//                });
//            }
//
//
            if (cardView != null ){
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null){
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION){
                                mListener.onItemClick(position);
                            }
                        }
                    }
                });
            }
//


        }

    }


}



