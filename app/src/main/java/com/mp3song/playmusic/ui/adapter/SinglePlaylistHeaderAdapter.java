package com.mp3song.playmusic.ui.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.databinding.ItemSinglePlaylistHeaderBinding;
import com.mp3song.playmusic.helpers.EventListener;
import com.mp3song.playmusic.helpers.Reliable;
import com.mp3song.playmusic.helpers.ReliableEvent;
import com.mp3song.playmusic.ui.AddMultipleSong;
import com.mp3song.playmusic.ui.fragments.HomeFragment;
import com.mp3song.playmusic.ui.viewmodel.SinglePlaylistViewModel;

import java.util.List;

public class SinglePlaylistHeaderAdapter extends RecyclerView.Adapter<SinglePlaylistHeaderAdapter.HeaderViewHolder> {
    public static final String ACTION_CLICK_MENU = "action-click-menu";
    public static final String ACTION_CLICK_PLAY_ALL = "action-click-play-all";
    public static final String ACTION_CLICK_SHUFFLE = "action-click-shuffle";

    private SinglePlaylistViewModel.State mState;
    private EventListener<SinglePlaylistViewModel.State> mEventListener;

    public SinglePlaylistViewModel.State getData() {
        return mState;
    }

    public boolean isAdded = true;

    public void setData(SinglePlaylistViewModel.State state, boolean isAdded) {
        mState = state;
        this.isAdded = isAdded;
        notifyItemChanged(0);
    }

    public EventListener<SinglePlaylistViewModel.State> getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener<SinglePlaylistViewModel.State> eventListener) {
        mEventListener = eventListener;
    }

    @NonNull
    @Override
    public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSinglePlaylistHeaderBinding singlePlaylistHeaderBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_single_playlist_header, parent, false);
        return new HeaderViewHolder(singlePlaylistHeaderBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull HeaderViewHolder holder, int position) {
        holder.bind(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull HeaderViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    final class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final ItemSinglePlaylistHeaderBinding singlePlaylistHeaderBinding;

        public HeaderViewHolder(ItemSinglePlaylistHeaderBinding singlePlaylistHeaderBinding) {
            super(singlePlaylistHeaderBinding.getRoot());

            this.singlePlaylistHeaderBinding = singlePlaylistHeaderBinding;
            singlePlaylistHeaderBinding.imgPlayAll.setOnClickListener(this::clickPlayAll);
            singlePlaylistHeaderBinding.imgShuffleAll.setOnClickListener(this::clickShuffle);
            singlePlaylistHeaderBinding.imgAdd.setOnClickListener(v -> {
                Intent intent = new Intent(singlePlaylistHeaderBinding.getRoot().getContext(), AddMultipleSong.class);
                intent.putExtra("playlist", mState.mPlaylist.id);
                singlePlaylistHeaderBinding.getRoot().getContext().startActivity(intent);
            });
            singlePlaylistHeaderBinding.back.setOnClickListener(v -> {
                HomeFragment sf = HomeFragment.newInstance();
                MainActivity activity1 = (MainActivity) singlePlaylistHeaderBinding.getRoot().getContext();
                activity1.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(null).commit();
            });
        }

        void clickMenu(View v) {
            if (getEventListener() != null) {
                getEventListener().handleEvent(new ReliableEvent<>(Reliable.success(null), ACTION_CLICK_MENU));
            }
        }

        void clickPlayAll(View v) {
            if (getEventListener() != null) {
                getEventListener().handleEvent(new ReliableEvent<>(Reliable.success(null), ACTION_CLICK_PLAY_ALL));
            }
        }

        void clickShuffle(View v) {
            if (getEventListener() != null) {
                getEventListener().handleEvent(new ReliableEvent<>(Reliable.success(null), ACTION_CLICK_SHUFFLE));
            }
        }

        public void bind(HeaderViewHolder holder) {
            Log.e("LLLL_Rename: ", mState.mTitle);
            if (isAdded)
                singlePlaylistHeaderBinding.imgAdd.setVisibility(View.VISIBLE);
            else
                singlePlaylistHeaderBinding.imgAdd.setVisibility(View.INVISIBLE);
            singlePlaylistHeaderBinding.title.setText(mState == null ? "" : mState.mTitle);
            Glide.with(holder.singlePlaylistHeaderBinding.getRoot().getContext())
                    .load(mState.mCoverImage)
                    .placeholder(R.drawable.ic_default_album_art)
                    .error(R.drawable.ic_default_album_art)
                    .into(singlePlaylistHeaderBinding.icon);
        }

        public void back() {

        }
    }
}
