package com.mp3song.playmusic.models.smartplaylist;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mp3song.playmusic.R;
import com.mp3song.playmusic.medialoader.SongLoader;
import com.mp3song.playmusic.models.Song;

import java.util.ArrayList;

public class ShuffleAllPlaylist extends AbsSmartPlaylist {

    public static final Parcelable.Creator<ShuffleAllPlaylist> CREATOR = new Parcelable.Creator<ShuffleAllPlaylist>() {
        public ShuffleAllPlaylist createFromParcel(Parcel source) {
            return new ShuffleAllPlaylist(source);
        }

        public ShuffleAllPlaylist[] newArray(int size) {
            return new ShuffleAllPlaylist[size];
        }
    };

    public ShuffleAllPlaylist(@NonNull Context context) {
        super(context.getString(R.string.action_shuffle_all), R.drawable.ic_shuffle);
    }

    protected ShuffleAllPlaylist(Parcel in) {
        super(in);
    }

    @NonNull
    @Override
    public ArrayList<Song> getSongs(@NonNull Context context) {
        return SongLoader.getAllSongs(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        // Shuffle all is not a real "Smart Playlist"
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
