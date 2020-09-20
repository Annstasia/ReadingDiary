package com.example.readingdiary.adapters;

//package com.example.galeryproject;

//package com.example.readingdiary;

//package com.example.readingdiary;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.ImageClass;
import com.example.readingdiary.R;
import com.squareup.picasso.Picasso;

import java.util.List;

// Адаптер поолнога показа изображений
public class GaleryFullViewAdapter extends RecyclerView.Adapter<GaleryFullViewAdapter.ViewHolder>{

    private List<ImageClass> buttons;
    private GaleryFullViewAdapter.OnItemClickListener mListener;
    private GaleryFullViewAdapter.OnItemDeleteListener delListener;
    private Context context;
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public interface OnItemDeleteListener{
        void onItemDelete(int position);
    }


    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener){delListener = listener;}



    public GaleryFullViewAdapter(List<ImageClass> buttons, Context context) {
        this.buttons = buttons;
        this.context = context;
        Log.d("BUTTONS", "1");

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.galery_full_view_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        Log.d("BUTTONS", "2");
//        v.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final int el = i;
        if (buttons.get(i).getType()==0){
            Bitmap source = buttons.get(i).getBitmap();
            viewHolder.imageView.setImageBitmap(source);
        }
        else{
            Picasso.get()
                    .load(buttons.get(i).getUri())
                    .into(viewHolder.imageView);
        }


    }


    @Override
    public int getItemCount() {
        return buttons.size();
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void clearAdapter() {
        buttons.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            Log.d("BUTTONS", "4");
            imageView = (ImageView) itemView.findViewById(R.id.galery_image_el1);
            itemView.setOnClickListener(new View.OnClickListener() {
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

    }


}



