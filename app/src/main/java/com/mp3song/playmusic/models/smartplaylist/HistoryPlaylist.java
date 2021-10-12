package com.mp3song.playmusic.models.smartplaylist;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mp3song.playmusic.R;
import com.mp3song.playmusic.medialoader.TopAndRecentlyPlayedTracksLoader;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.provider.HistoryStore;
import com.mp3song.playmusic.utils.MusicUtil;
import com.mp3song.playmusic.utils.PreferenceUtil;

import java.util.ArrayList;

public class HistoryPlaylist extends AbsSmartPlaylist {

    public static final Parcelable.Creator<HistoryPlaylist> CREATOR = new Parcelable.Creator<HistoryPlaylist>() {
        public HistoryPlaylist createFromParcel(Parcel source) {
            return new HistoryPlaylist(source);
        }

        public HistoryPlaylist[] newArray(int size) {
            return new HistoryPlaylist[size];
        }
    };

    public HistoryPlaylist(@NonNull Context context) {
        super(context.getString(R.string.history), R.drawable.ic_access_time_white_24dp);
    }

    protected HistoryPlaylist(Parcel in) {
        super(in);
    }

    @NonNull
    @Override
    public String getInfoString(@NonNull Context context) {
        String cutoff = PreferenceUtil.getInstance(context).getRecentlyPlayedCutoffText(context);

        return MusicUtil.buildInfoString(
                cutoff,
                super.getInfoString(context)
        );
    }

    @NonNull
    @Override
    public ArrayList<Song> getSongs(@NonNull Context context) {
        return TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        HistoryStore.getInstance(context).clear();
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
