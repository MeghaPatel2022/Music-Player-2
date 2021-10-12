package com.mp3song.playmusic;

import android.os.AsyncTask;
import android.os.Bundle;
import android.service.carrier.CarrierMessagingService;
import android.util.Log;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.mp3song.playmusic.contract.AbsMediaAdapter;
import com.mp3song.playmusic.databinding.ActivityArtistBinding;
import com.mp3song.playmusic.models.Artist;
import com.mp3song.playmusic.ui.MusicServiceActivity;
import com.mp3song.playmusic.ui.adapter.Library.artist.SongInArtistPagerAdapter;
import com.mp3song.playmusic.utils.imageload.ArtistGlideRequest;

import java.lang.ref.WeakReference;

public class ArtistActivity extends MusicServiceActivity {

    private static final String TAG = "ArtistPagerActivity";
    private static final String ARTIST = "artist";
    private final SongInArtistPagerAdapter mAdapter = new SongInArtistPagerAdapter("a1");
    private ActivityArtistBinding artistBinding;
    private Artist mArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artistBinding = DataBindingUtil.setContentView(ArtistActivity.this, R.layout.activity_artist);

        mAdapter.init(ArtistActivity.this);
        mAdapter.setName(TAG);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mArtist = bundle.getParcelable(ARTIST);
        }

        artistBinding.recyclerView.setAdapter(mAdapter);
        artistBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ArtistActivity.this, LinearLayoutManager.VERTICAL, false));

        refreshData();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
    }

    private void updateSongs() {
        if (mArtist == null) return;
        mAdapter.setData(mArtist.getSongs());
    }

    public void refreshData() {
        if (mArtist == null) return;
        artistBinding.title.setText(mArtist.getName());
        String bio = "";
        if (!bio.isEmpty()) bio = ' ' + getResources().getString(R.string.middle_dot) + ' ' + bio;
        artistBinding.description.setText(mArtist.getSongCount() + " " + getResources().getString(R.string.songs) + bio);


        ArtistGlideRequest.Builder.from(Glide.with(ArtistActivity.this), mArtist)
                .tryToLoadOriginal(true)
                .generateBuilder(ArtistActivity.this)
                .build()
                .error(R.drawable.ic_artist)
                .placeholder(R.drawable.ic_artist)
                .thumbnail(
                        ArtistGlideRequest
                                .Builder
                                .from(Glide.with(ArtistActivity.this), mArtist)
                                .generateBuilder(ArtistActivity.this)
                                .build())
                .into(artistBinding.bigImage);

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