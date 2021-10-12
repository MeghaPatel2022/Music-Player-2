package com.mp3song.playmusic.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mp3song.playmusic.R;
import com.mp3song.playmusic.medialoader.SongLoader;
import com.mp3song.playmusic.models.Playlist;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.ui.adapter.MultipleSongAdapter;
import com.mp3song.playmusic.utils.PlaylistsUtil;

import java.util.ArrayList;

public class AddMultipleSong extends AppCompatActivity {

    MultipleSongAdapter adapter;
    ArrayList<Song> songArrayList = new ArrayList<>();
    ArrayList<Song> selectedList = new ArrayList<>();
    Playlist mPlaylist;
    int playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.mp3song.playmusic.databinding.ActivityAddMultipleSongBinding addMultipleSongBinding = DataBindingUtil.setContentView(AddMultipleSong.this, R.layout.activity_add_multiple_song);

        playlist = getIntent().getIntExtra("playlist", 0);
        songArrayList = SongLoader.getAllSongs(AddMultipleSong.this);

        Log.e("LLL_ID: ", String.valueOf(playlist));

        adapter = new MultipleSongAdapter(songArrayList, AddMultipleSong.this);
        addMultipleSongBinding.recyclerView.setHasFixedSize(true);
        addMultipleSongBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        addMultipleSongBinding.recyclerView.setAdapter(adapter);

        addMultipleSongBinding.imgAdd.setOnClickListener(v -> {
            for (Song model : songArrayList) {
                if (model.isSelected()) {
                    selectedList.add(model);
                }
            }
            Log.e("LLL_size: ", String.valueOf(selectedList.size()));
            PlaylistsUtil.addToPlaylist(AddMultipleSong.this, selectedList, playlist, true);

            onBackPressed();
        });

        addMultipleSongBinding.imgBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}