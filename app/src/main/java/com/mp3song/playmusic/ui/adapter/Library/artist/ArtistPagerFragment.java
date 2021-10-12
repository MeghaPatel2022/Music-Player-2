package com.mp3song.playmusic.ui.adapter.Library.artist;

import android.os.AsyncTask;
import android.os.Bundle;
import android.service.carrier.CarrierMessagingService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.contract.AbsMediaAdapter;
import com.mp3song.playmusic.models.Artist;
import com.mp3song.playmusic.ui.MusicServiceNavigationFragment;
import com.mp3song.playmusic.ui.NavigationFragment;
import com.mp3song.playmusic.ui.fragments.HomeFragment;
import com.mp3song.playmusic.ui.fragments.LibraryTabFragment;
import com.mp3song.playmusic.utils.Util;
import com.mp3song.playmusic.utils.imageload.ArtistGlideRequest;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ArtistPagerFragment extends MusicServiceNavigationFragment {
    private static final String TAG = "ArtistPagerFragment";
    private static final String ARTIST = "artist";
    private final SongInArtistPagerAdapter mAdapter = new SongInArtistPagerAdapter("ar1");
    private final boolean mBlockPhotoView = true;
    @BindView(R.id.status_bar)
    View mStatusBar;
    @BindView(R.id.title)
    TextView mArtistText;
    @BindView(R.id.big_image)
    RoundedImageView mBigImage;
    @BindView(R.id.description)
    TextView mWiki;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    private Artist mArtist;
    private Unbinder mUnbinder;

    public static ArtistPagerFragment newInstance(Artist artist) {

        Bundle args = new Bundle();
        if (artist != null)
            args.putParcelable(ARTIST, artist);

        ArtistPagerFragment fragment = new ArtistPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static boolean handleBackPressed(FragmentManager fm) {
        if (fm.getFragments() != null) {
            for (Fragment frag : fm.getFragments()) {
                if (frag != null && frag.isVisible() && frag instanceof ArtistPagerFragment) {
                    if (((ArtistPagerFragment) frag).onBackPressed()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onSetStatusBarMargin(int value) {
        mStatusBar.getLayoutParams().height = value;
        mStatusBar.requestLayout();
    }

    @OnClick(R.id.preview_button)
    void previewAllSong() {
        mAdapter.previewAll(true);
    }

    @OnClick(R.id.back)
    void goBack() {
        Util.lastPos = 2;
        NavigationFragment sf = LibraryTabFragment.newInstance();
        String backStateName = HomeFragment.class.getName();
        MainActivity activity = (MainActivity) requireContext();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
    }

    @OnClick(R.id.play)
    void shuffle() {
        mAdapter.shuffle();
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

    protected boolean onBackPressed() {
        FragmentManager fm = getChildFragmentManager();
        if (handleBackPressed(fm)) {
            return true;
        } else if (getUserVisibleHint() && fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.screen_single_artist_primary, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mArtist = bundle.getParcelable(ARTIST);
        }

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                Util.lastPos = 2;
                NavigationFragment sf = LibraryTabFragment.newInstance();
                String backStateName = HomeFragment.class.getName();
                MainActivity activity = (MainActivity) requireContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
                return true;
            }
            return false;
        });

        refreshData();
    }

    private void updateSongs() {
        if (mArtist == null) return;
        mAdapter.setData(mArtist.getSongs());
    }

    public void refreshData() {
        if (mArtist == null) return;
        mArtistText.setText(mArtist.getName());
        String bio = "";
        if (!bio.isEmpty()) bio = ' ' + getResources().getString(R.string.middle_dot) + ' ' + bio;
        mWiki.setText(mArtist.getSongCount() + " " + getResources().getString(R.string.songs) + bio);

        if (getContext() != null) {
            ArtistGlideRequest.Builder.from(Glide.with(getContext()), mArtist)
                    .tryToLoadOriginal(true)
                    .generateBuilder(getContext())
                    .build()
                    .error(R.drawable.ic_artist)
                    .placeholder(R.drawable.ic_artist)
                    .thumbnail(
                            ArtistGlideRequest
                                    .Builder
                                    .from(Glide.with(getContext()), mArtist)
                                    .generateBuilder(getContext())
                                    .build())
                    .into(mBigImage);
        }
        updateSongs();
    }

    @Override
    public void onServiceConnected() {
        refreshData();
    }


    @Override
    public void onPlayingMetaChanged() {
        Log.d(TAG, "onPlayingMetaChanged");
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
        Log.d(TAG, "onPlayStateChanged");
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

    private static class ArtistInfoTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<CarrierMessagingService.ResultCallback> mCallback;

        ArtistInfoTask(CarrierMessagingService.ResultCallback callback) {
            mCallback = new WeakReference<>(callback);
        }

        void cancel() {
            cancel(true);
            mCallback.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }
    }
}
