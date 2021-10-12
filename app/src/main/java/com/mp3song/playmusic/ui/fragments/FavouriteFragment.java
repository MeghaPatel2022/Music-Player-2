package com.mp3song.playmusic.ui.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mp3song.playmusic.App;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.contract.AbsMediaAdapter;
import com.mp3song.playmusic.databinding.FragmentFavouriteBinding;
import com.mp3song.playmusic.models.Playlist;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.ui.MusicServiceNavigationFragment;
import com.mp3song.playmusic.ui.PresentStyle;
import com.mp3song.playmusic.ui.adapter.SongChildAdapter;
import com.mp3song.playmusic.utils.SharedPrefrences;
import com.mp3song.playmusic.utils.Tool;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FavouriteFragment extends MusicServiceNavigationFragment {

    private final SongChildAdapter mAdapter = new SongChildAdapter();

    private FragmentFavouriteBinding favouriteBinding;

    SongMiniAdapter mSongMiniAdapter;
    Playlist mPlaylist;
    ViewGroup springBackLayout;
    Bitmap mPreviewBitmap;
    private Unbinder mUnbinder;
    private int mCurrentSortOrder = 0;

    private RefreshReceiver refreshReceiver;

    public static FavouriteFragment newInstance() {
        FavouriteFragment fragment = new FavouriteFragment();

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        favouriteBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_favourite, container, false);
        return favouriteBinding.getRoot();
    }

    @Override
    public int getPresentTransition() {
        return PresentStyle.ACCORDION_LEFT;
    }

    public void setTheme() {
        int heavyColor = Tool.getHeavyColor();

        favouriteBinding.favRecycler.setPopupBgColor(heavyColor);
        favouriteBinding.favRecycler.setThumbColor(heavyColor);
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayingMetaChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        setTheme();
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {

    }

    @Override
    public void onDestroyView() {
        mAdapter.destroy();

        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        initSortOrder();
        setTheme();

        if (mPreviewBitmap != null) {

            mPreviewBitmap = null;
        }

        refreshReceiver = new RefreshReceiver();

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshReceiver,
                new IntentFilter("REFRESH_FAV"));

        mSongMiniAdapter = new SongMiniAdapter(springBackLayout);
        onBack();
        favouriteBinding.back.setOnClickListener(v -> {
            HomeFragment sf = HomeFragment.newInstance();
            MainActivity activity = (MainActivity) requireContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(null).commit();
        });
    }

    private void onBack() {
        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                HomeFragment sf = HomeFragment.newInstance();
                MainActivity activity = (MainActivity) requireContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(null).commit();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setSuggestedSongs(SharedPrefrences.getFavouriteList(getContext()));
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    }


    public void setSuggestedSongs(List<Song> song) {
        mSongMiniAdapter.bind(song);
    }

    @Override
    public void onSetStatusBarMargin(int value) {

    }

    private void initSortOrder() {
        if (mPlaylist != null && !mPlaylist.name.isEmpty()) {
            int defaultOrder = 0;
            if (mPlaylist.name.equals(getResources().getString(R.string.playlist_last_added)))
                defaultOrder = 2;
            mCurrentSortOrder = App.getInstance().getPreferencesUtility().getSharePreferences().getInt("sort_order_playlist_" + mPlaylist.name + "_" + mPlaylist.id, defaultOrder);
        }
    }


    public class SongMiniAdapter {
        private final View mItemView;

        SongMiniAdapter(View v) {
            this.mItemView = v;
            ButterKnife.bind(this, v);
            mAdapter.init(getContext());
            favouriteBinding.favRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            favouriteBinding.favRecycler.setAdapter(mAdapter);

        }

        @SuppressLint("DefaultLocale")
        public void bind(List<Song> playlists) {
            if (playlists != null && playlists.size() > 0) {
                favouriteBinding.favRecycler.setVisibility(View.VISIBLE);
                favouriteBinding.noData.setVisibility(View.GONE);
                mAdapter.setData(playlists);
            } else {
                favouriteBinding.favRecycler.setVisibility(View.GONE);
                favouriteBinding.noData.setVisibility(View.VISIBLE);
            }
        }

        public void notifyDataSetChanged() {
            mAdapter.notifyDataSetChanged();
        }

        public int getItemCount() {
            return mAdapter.getItemCount();
        }
    }

}