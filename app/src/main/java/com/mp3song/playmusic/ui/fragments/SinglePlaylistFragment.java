package com.mp3song.playmusic.ui.fragments;

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.contract.AbsMediaAdapter;
import com.mp3song.playmusic.databinding.ScreenSinglePlaylist2Binding;
import com.mp3song.playmusic.helpers.EventListener;
import com.mp3song.playmusic.helpers.Reliable;
import com.mp3song.playmusic.helpers.ReliableEvent;
import com.mp3song.playmusic.models.Playlist;
import com.mp3song.playmusic.ui.MusicServiceNavigationFragment;
import com.mp3song.playmusic.ui.PresentStyle;
import com.mp3song.playmusic.ui.adapter.SinglePlaylistHeaderAdapter;
import com.mp3song.playmusic.ui.adapter.SongChildAdapter;
import com.mp3song.playmusic.ui.base.MPViewModel;
import com.mp3song.playmusic.ui.viewmodel.SinglePlaylistViewModel;
import com.mp3song.playmusic.utils.Util;

import org.jetbrains.annotations.NotNull;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SinglePlaylistFragment extends MusicServiceNavigationFragment implements EventListener<SinglePlaylistViewModel.State> {

    public static final String PLAYLIST = "playlist";
    public static final String ISADDED = "isAdded";
    private final SongChildAdapter mAdapter = new SongChildAdapter("playList");
    private final SinglePlaylistHeaderAdapter mHeaderAdapter = new SinglePlaylistHeaderAdapter();
    Bitmap mPreviewBitmap;
    private ScreenSinglePlaylist2Binding singlePlaylist2Binding;
    private SinglePlaylistViewModel mViewModel;
    private ConcatAdapter mConcatAdapter;
    private Unbinder mUnbinder;

    private RefreshPlayListReceiver refreshPlayListReceiver;

    void back() {
        if (MainActivity.mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            MainActivity.mainPopUpBehaviour.setHideable(true);
            MainActivity.mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            HomeFragment sf = HomeFragment.newInstance();
            MainActivity activity = (MainActivity) requireContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(null).commit();
        }
    }

    public static void animateAndChangeImageView(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public static SinglePlaylistFragment newInstance(boolean isAdded, Playlist playlist, @Nullable Bitmap previewBitmap) {
        SinglePlaylistFragment fragment = new SinglePlaylistFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PLAYLIST, playlist);
        bundle.putBoolean(ISADDED, isAdded);
        fragment.setArguments(bundle);
        fragment.mPreviewBitmap = previewBitmap;
        return fragment;
    }

    @Override
    public int getPresentTransition() {
        return PresentStyle.ACCORDION_LEFT;
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
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        singlePlaylist2Binding.recyclerView.setAdapter(mConcatAdapter);
        singlePlaylist2Binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        singlePlaylist2Binding.swipeRefresh.setEnabled(false);
        singlePlaylist2Binding.swipeRefresh.setColorSchemeResources(R.color.flatOrange);
        singlePlaylist2Binding.swipeRefresh.setOnRefreshListener(this::refreshData);

        singlePlaylist2Binding.back.setOnClickListener(v -> back());

        refreshPlayListReceiver = new RefreshPlayListReceiver();

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshPlayListReceiver,
                new IntentFilter("REFRESH_PLAYLIST_WHOLE"));

        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((v, keyCode, event1) -> {
            if (event1.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (MainActivity.mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    MainActivity.mainPopUpBehaviour.setHideable(true);
                    MainActivity.mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else if (MainActivity.mainPopUpBehaviour1.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    MainActivity.mainPopUpBehaviour1.setHideable(true);
                    MainActivity.mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else if (MainActivity.mainPopUpBehaviour2.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    MainActivity.mainPopUpBehaviour2.setHideable(true);
                    MainActivity.mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else {
                    HomeFragment sf = HomeFragment.newInstance();
                    MainActivity activity = (MainActivity) requireContext();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(null).commit();
                }
                return true;
            }
            return false;
        });

    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mConcatAdapter = new ConcatAdapter(mHeaderAdapter, mAdapter);
        mHeaderAdapter.setEventListener(this);

        mViewModel = ViewModelProviders.of(this).get(SinglePlaylistViewModel.class);
        ReliableEvent<SinglePlaylistViewModel.State> event = mViewModel.getStateLiveData().getValue();
        if (event == null || event.getReliable().getData() == null) {
            SinglePlaylistViewModel.State state = new SinglePlaylistViewModel.State();
            Bundle bundle = getArguments();
            state.mPlaylist = bundle == null ? null : bundle.getParcelable(PLAYLIST);
            if (state.mPlaylist != null)
                Util.playListId = state.mPlaylist.id;
            state.mCoverImage = mPreviewBitmap;
            mViewModel.getStateLiveData().setValue(new ReliableEvent<>(Reliable.success(state), MPViewModel.ACTION_SET_PARAMS));
        }
        mViewModel.getStateLiveData().observe(this, _event -> {
            SinglePlaylistViewModel.State state = _event == null ? null : _event.getReliable().getData();
            Bundle bundle = getArguments();
            boolean isAdd = bundle != null && bundle.getBoolean(ISADDED);
            mHeaderAdapter.setData(state, isAdd);
            mAdapter.setData(state == null ? null : state.songs);
        });
    }

    private void refreshData() {
        mViewModel.refreshData();
    }

    @Override
    public boolean handleEvent(ReliableEvent<SinglePlaylistViewModel.State> event, SinglePlaylistViewModel.State data) {
        String action = event.getAction();
        if (action == null) {
            return false;
        }
        switch (action) {
            case SinglePlaylistHeaderAdapter.ACTION_CLICK_MENU:
//                onClickMenu();
                break;
            case SinglePlaylistHeaderAdapter.ACTION_CLICK_PLAY_ALL:
                mAdapter.playAll(0, true);
                break;
            case SinglePlaylistHeaderAdapter.ACTION_CLICK_SHUFFLE:
                mAdapter.shuffle();
                break;
        }
        return true;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        singlePlaylist2Binding = DataBindingUtil.inflate(inflater, R.layout.screen_single_playlist_2, container, false);

        return singlePlaylist2Binding.getRoot();
    }

    private class RefreshPlayListReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshData();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onServiceConnected() {
        refreshData();
    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {
        refreshData();
    }

    @Override
    public void onPlayingMetaChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        //setTheme();
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
        refreshData();
    }


}
