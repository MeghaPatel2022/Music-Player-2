package com.mp3song.playmusic.ui.fragments.album;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.service.carrier.CarrierMessagingService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.contract.AbsMediaAdapter;
import com.mp3song.playmusic.models.Album;
import com.mp3song.playmusic.ui.MusicServiceNavigationFragment;
import com.mp3song.playmusic.ui.NavigationFragment;
import com.mp3song.playmusic.ui.adapter.Library.artist.SongInArtistPagerAdapter;
import com.mp3song.playmusic.ui.fragments.HomeFragment;
import com.mp3song.playmusic.ui.fragments.LibraryTabFragment;
import com.mp3song.playmusic.utils.Util;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import es.dmoral.toasty.Toasty;

public class AlbumPagerFragment extends MusicServiceNavigationFragment {
    private static final String TAG = "ArtistPagerFragment";
    private static final String ARTIST = "album";
    private final boolean mBlockPhotoView = true;
    private final SongInArtistPagerAdapter mAdapter = new SongInArtistPagerAdapter("pl");

    @BindView(R.id.title)
    TextView mArtistText;
    @BindView(R.id.big_image)
    ImageView icon;
    @BindView(R.id.description)
    TextView mWiki;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.preview_button)
    ImageView preview_button;

    private Album mAlbum;

    private Unbinder mUnbinder;

    public static AlbumPagerFragment newInstance(Album artist) {

        Bundle args = new Bundle();
        if (artist != null)
            args.putParcelable(ARTIST, artist);

        AlbumPagerFragment fragment = new AlbumPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @OnClick(R.id.back)
    void goBack() {
        Util.lastPos = 1;
        NavigationFragment sf = LibraryTabFragment.newInstance();
        String backStateName = HomeFragment.class.getName();
        MainActivity activity = (MainActivity) requireContext();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
    }

    @OnClick(R.id.play)
    void shuffle() {
        Toasty.info(getActivity(), "Start playing all.", Toast.LENGTH_SHORT).show();
        mAdapter.playAll(0, true);
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
            mAlbum = bundle.getParcelable(ARTIST);
        }

        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri,
                mAlbum.getId());

        Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(mAlbum.getId())},
                null);

        if (cursor.moveToFirst()) {
            Glide.with(getContext())
                    .asBitmap()
                    .load(uri)
                    .error(R.drawable.ic_default_album_art)
                    .placeholder(R.drawable.ic_default_album_art)
                    .into(icon);
        }

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        preview_button.setVisibility(View.GONE);
        refreshData();

        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                Util.lastPos = 1;
                NavigationFragment sf = LibraryTabFragment.newInstance();
                String backStateName = HomeFragment.class.getName();
                MainActivity activity = (MainActivity) requireContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
                return true;
            }
            return false;
        });
    }

    private void updateSongs() {
        if (mAlbum == null) return;
        mAdapter.setData(mAlbum.getSongs());
    }

    public void refreshData() {
        if (mAlbum == null) return;
        mArtistText.setText(mAlbum.getTitle());
        String bio = "";
        if (!bio.isEmpty()) bio = ' ' + getResources().getString(R.string.middle_dot) + ' ' + bio;
        mWiki.setText(mAlbum.getSongCount() + " " + getResources().getString(R.string.songs) + bio);
        icon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_default_album_art));
        updateSongs();
    }

    @Override
    public void onServiceConnected() {
        refreshData();
    }


    @Override
    public void onPlayingMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        if (mRecyclerView instanceof FastScrollRecyclerView) {
            FastScrollRecyclerView recyclerView = ((FastScrollRecyclerView) mRecyclerView);
            recyclerView.setPopupBgColor(getResources().getColor(R.color.background_color));
            recyclerView.setThumbColor(getResources().getColor(R.color.background_color));
        }
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
        Log.d(TAG, "onPlayStateChanged");
        if (mAdapter != null)
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
