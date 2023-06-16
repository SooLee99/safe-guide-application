package com.example.safe_guide.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe_guide.R;
import com.example.safe_guide.model.BoardListModel;

import java.util.ArrayList;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.CustomViewHolder> {

    private ArrayList<BoardListModel> boardListModels;

    public BoardAdapter(ArrayList<BoardListModel> boardListModels) {
        this.boardListModels = boardListModels;
    }

    @NonNull
    @Override
    public BoardAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_list, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BoardAdapter.CustomViewHolder holder, int position) {
        holder.tvTitle.setText(boardListModels.get(position).getTitle());
        holder.tvName.setText(boardListModels.get(position).getName());
        holder.tvContent.setText(boardListModels.get(position).getContent());

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != boardListModels ? boardListModels.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvTitle;
        protected TextView tvName;
        protected TextView tvContent;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvTitle = itemView.findViewById(R.id.tvTitle);
            this.tvName = itemView.findViewById(R.id.tvName);
            this.tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
