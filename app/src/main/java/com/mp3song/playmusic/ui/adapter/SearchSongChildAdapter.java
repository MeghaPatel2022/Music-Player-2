package com.mp3song.playmusic.ui.adapter;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp3song.playmusic.R;
import com.mp3song.playmusic.contract.AbsBindAbleHolder;
import com.mp3song.playmusic.contract.AbsSongAdapter;
import com.mp3song.playmusic.helpers.SongMenuHelper;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.service.MusicPlayerRemote;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SearchSongChildAdapter extends AbsSongAdapter
        implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = "SongChildAdapter";
    private final Random mRandom = new Random();
    private final String mFrom = "";
    public int mRandomItem = 0;
    public int MEDIA_LAYOUT_RESOURCE = R.layout.item_song_normal;
    List<Song> mData = new ArrayList<>();
    List<Song> mFilterList = new ArrayList<>();
    private int[] mOptionRes = SongMenuHelper.SONG_OPTION;

    public SearchSongChildAdapter() {
        super();
    }

    public void setDataAdapter(List<Song> mData) {
        setData(mData);
        mFilterList = mData;
    }

    @Override
    protected void onDataSet() {
        super.onDataSet();
//        randomize();
    }

    public void destroy() {
        super.destroy();
    }

    public void setSongOptionHelperRes(final int[] res) {
        mOptionRes = res;
    }

    @Override
    protected void onMenuItemClick(int positionInData) {
        if (mFrom.equals("playList")) {
            Log.e("LLL_Pos: ", String.valueOf(positionInData));
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
            Intent localIn;
            localIn = new Intent("REFRESH");
            localIn.putExtra("receive", "playListItem");
            localIn.putExtra("object", (Serializable) getData().get(positionInData));
            lbm.sendBroadcast(localIn);
        } else {
            Log.e("LLL_Pos: ", String.valueOf(positionInData));
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
            Intent localIn;
            localIn = new Intent("REFRESH");
            localIn.putExtra("receive", "songsItem");
            localIn.putExtra("object", (Serializable) getData().get(positionInData));
            lbm.sendBroadcast(localIn);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return MEDIA_LAYOUT_RESOURCE;
    }

    @Override
    protected int getMediaHolderPosition(int dataPosition) {
        return dataPosition;
    }

    @Override
    protected int getDataPosition(int itemHolderPosition) {
        return itemHolderPosition;
    }

    @Override
    public int getItemCount() {
        return getData().size();
    }

    @NotNull
    @Override
    public AbsBindAbleHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(viewType, viewGroup, false);

        return new SearchSongChildAdapter.ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull AbsBindAbleHolder itemHolder, int position) {
        itemHolder.bind(getData().get(getDataPosition(position)));
    }

    public void randomize() {
        if (getData().isEmpty()) return;
        mRandomItem = mRandom.nextInt(getData().size());
    }

    public void shuffle() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            MusicPlayerRemote.openQueue(getData(), mRandomItem, true);
            //MusicPlayer.playAll(mContext, mSongIDs, mRandomItem, -1, Util.IdType.NA, false);
            Handler handler1 = new Handler();
            handler1.postDelayed(() -> {
                notifyItemChanged(getMediaHolderPosition(mMediaPlayDataItem));
                notifyItemChanged(getMediaHolderPosition(mRandomItem));
                mMediaPlayDataItem = mRandomItem;
                randomize();
            }, 50);
        }, 100);
    }


    @NonNull
    @Override
    public String getSectionName(int position) {
        if (position == 0) return "A";
        if (getData().get(position).title.isEmpty())
            return "A";
        return getData().get(position).title.substring(0, 1);
    }

    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, int i) {
        return recyclerView.getResources().getDimensionPixelSize(R.dimen.item_song_child_height);
    }


    public class ItemHolder extends AbsSongAdapter.SongHolder {
        public ItemHolder(View view) {
            super(view);
        }
    }
}
