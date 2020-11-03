package com.example.readingdiary.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.ImageClass;
import com.example.readingdiary.Classes.SmallGaleryTransform;
import com.example.readingdiary.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.squareup.picasso.Picasso;

import java.util.List;

// Адаптер для GaleryActivity.
public class GaleryRecyclerViewAdapter extends RecyclerView.Adapter<GaleryRecyclerViewAdapter.ViewHolder>{

    private List<ImageClass> buttons;
    private GaleryRecyclerViewAdapter.OnItemClickListener mListener;
    private Context context;
    private boolean actionMode;
    public interface OnItemClickListener{
        void onItemClick(int position);
        void onItemLongClick(int position);
        void onCheckClick(int position);
        void onUncheckClick(int position);
    }


    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public GaleryRecyclerViewAdapter(List<ImageClass> buttons, Context context) {
        this.buttons = buttons;
        this.context = context;
        this.actionMode=false;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_galery_view, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
//        v.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        if (!actionMode){
            viewHolder.checkBox.setChecked(false);
            viewHolder.checkBox.setVisibility(View.GONE);
        }
        else{
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        }
        if (buttons.get(i).getType()==1){
            DisplayMetrics metricsB = context.getResources().getDisplayMetrics();
            float size = metricsB.widthPixels / 3;
            SmallGaleryTransform smallGaleryTransform = new SmallGaleryTransform(metricsB.widthPixels, metricsB.heightPixels);
            Picasso.get()
                    .load(buttons.get(i).getUri())
                    .transform(new SmallGaleryTransform(metricsB.widthPixels, metricsB.heightPixels))
                    .into(viewHolder.imageView);
        }
        else{
            Bitmap source = buttons.get(i).getBitmap();
            DisplayMetrics metricsB = context.getResources().getDisplayMetrics();
            float size = metricsB.widthPixels / 3;
            float size1 = Math.min(source.getWidth(), source.getHeight());
            float k = size / size1;
            Log.d("SCALE", k+" " + size + " " + size1);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(source, (int)(source.getWidth() * k), (int)(source.getHeight() * k), false);
            int x = (int)((resizedBitmap.getWidth() - size) / 2);
            int y = (int)((resizedBitmap.getHeight() - size) / 2);
            Bitmap result = Bitmap.createBitmap(resizedBitmap, x, y, (int)size, (int)size);

            viewHolder.imageView.setImageBitmap(result);
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

    public void setActionMode(boolean mode){
        actionMode = mode;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //        private TextView path1;
//        private TextView path2;
//
//        private TextView title;
//        private TextView author;
        private ImageView imageView;
        private MaterialCheckBox checkBox;

//        private ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.galery_image_el0);
            checkBox = (MaterialCheckBox) itemView.findViewById(R.id.galery_item_check);
//            title = (TextView) itemView.findViewById(R.id.titleViewCatalog);
//            author = (TextView) itemView.findViewById(R.id.authorViewCatalog);
//            path2 = (TextView) itemView.findViewById(R.id.pathViewCatalog1);

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
        }

    }


}



