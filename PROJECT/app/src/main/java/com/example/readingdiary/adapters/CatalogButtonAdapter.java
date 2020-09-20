package com.example.readingdiary.adapters;

//package com.example.readingdiary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.R;

import java.util.List;


// Адаптор путей (кнопочки, для быстрого перемещения назад)
public class CatalogButtonAdapter extends RecyclerView.Adapter<CatalogButtonAdapter.ViewHolder>{

    private List<String> buttons;
    private CatalogButtonAdapter.OnItemClickListener mListener;
    public interface OnItemClickListener{
        void onItemClick(int position);
    }


    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public CatalogButtonAdapter(List<String> buttons) {
        this.buttons = buttons;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_catalog_button, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        String tokens[] = buttons.get(i).split("/");
        if (i == 0){
            viewHolder.path1.setText("Папки");
        }
        else{
            viewHolder.path1.setText(tokens[tokens.length - 1]);
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
        private TextView path1;
        public ViewHolder(View itemView) {
            super(itemView);
            path1 = (TextView) itemView.findViewById(R.id.catalog_button);

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



