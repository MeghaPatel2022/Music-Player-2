package com.mp3song.playmusic.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.utils.Util;

import java.util.ArrayList;

public class MultipleSongAdapter extends RecyclerView.Adapter<MultipleSongAdapter.ViewHolder> {

    ArrayList<Song> songArrayList = new ArrayList<>();
    Context context;

    public MultipleSongAdapter(ArrayList<Song> songArrayList, Context context) {
        this.songArrayList = songArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multiple_song, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.mTitle.setText(songArrayList.get(position).title);
        holder.mDescription.setText(songArrayList.get(position).artistName);

        Glide.with(context).load(Util.getAlbumArtUri(songArrayList.get(position).albumId))
                .placeholder(R.drawable.ic_default_album_art)
                .error(R.drawable.ic_default_album_art)
                .into(holder.mImage);
        final Song song = songArrayList.get(position);
        holder.checkButton.setSelected(song.isSelected());
        holder.itemView.setOnClickListener(v -> {
            holder.checkButton.setSelected(true);
            song.setSelected(!song.isSelected());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle, mDescription;
        ImageView mImage, checkButton;

        public ViewHolder(View view) {
            super(view);

            mTitle = view.findViewById(R.id.title);
            mDescription = view.findViewById(R.id.description);
            mImage = view.findViewById(R.id.image);
            checkButton = view.findViewById(R.id.checkButton);
        }

    }

}
