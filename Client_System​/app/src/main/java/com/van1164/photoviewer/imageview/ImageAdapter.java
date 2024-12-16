package com.van1164.photoviewer.imageview;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.van1164.photoviewer.R;
import com.van1164.photoviewer.dto.PhotoResponseDto;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<PhotoResponseDto> photoResponseDtoList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(PhotoResponseDto photo);
    }

    public ImageAdapter(List<PhotoResponseDto> photoResponseDtoList, OnItemClickListener listener) {
        this.photoResponseDtoList = photoResponseDtoList;
        this.onItemClickListener = listener;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        PhotoResponseDto photo = photoResponseDtoList.get(position);
        holder.imageView.setImageBitmap(photo.getBitmap());
        holder.textViewTitle.setText(photo.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(photo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoResponseDtoList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewTitle;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
        }
    }
}