package com.mp3song.playmusic.models.smartplaylist;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mp3song.playmusic.R;
import com.mp3song.playmusic.medialoader.TopAndRecentlyPlayedTracksLoader;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.provider.SongPlayCountStore;

import java.util.ArrayList;

/**
 * @author Megha Patel
 */
public class MyTopTracksPlaylist extends AbsSmartPlaylist {

    public static final Parcelable.Creator<MyTopTracksPlaylist> CREATOR = new Parcelable.Creator<MyTopTracksPlaylist>() {
        public MyTopTracksPlaylist createFromParcel(Parcel source) {
            return new MyTopTracksPlaylist(source);
        }

        public MyTopTracksPlaylist[] newArray(int size) {
            return new MyTopTracksPlaylist[size];
        }
    };

    public MyTopTracksPlaylist(@NonNull Context context) {
        super(context.getString(R.string.playlist_top_tracks), R.drawable.ic_trending_up_white_24dp);
    }

    protected MyTopTracksPlaylist(Parcel in) {
        super(in);
    }

    @NonNull
    @Override
    public ArrayList<Song> getSongs(@NonNull Context context) {
        return TopAndRecentlyPlayedTracksLoader.getTopTracks(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        SongPlayCountStore.getInstance(context).clear();
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
