package com.mp3song.playmusic.ui.adapter.Library.artist;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mp3song.playmusic.R;
import com.mp3song.playmusic.ui.adapter.SongChildAdapter;

import java.io.Serializable;

public class SongInArtistPagerAdapter extends SongChildAdapter {
    private static final String TAG = "SongInArtistPagerAdapter";
    private String mFrom = "";

    public SongInArtistPagerAdapter(String mFrom) {
        super(mFrom);
        this.mFrom = mFrom;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_song_normal;
    }

    @Override
    protected void onMenuItemClick(int positionInData) {
        Log.e("LLL_From: ", mFrom);
        if (!mFrom.equals("playList")) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
            Intent localIn;
            localIn = new Intent("REFRESH");
            localIn.putExtra("receive", "songsItem");
            localIn.putExtra("object", (Serializable) getData().get(positionInData));
            lbm.sendBroadcast(localIn);
        }
        /*OptionBottomSheet
                .newInstance(SongMenuHelper.SONG_ARTIST_OPTION,getData().get(positionInData))
                .show(((AppCompatActivity)getContext()).getSupportFragmentManager(), "song_popup_menu");*/
    }
}
