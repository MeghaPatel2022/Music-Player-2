package com.mp3song.playmusic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;

import com.mp3song.playmusic.databinding.ActivityNowPlayingBinding;
import com.mp3song.playmusic.service.MusicPlayerRemote;
import com.mp3song.playmusic.service.MusicService;
import com.mp3song.playmusic.ui.MusicServiceActivity;
import com.mp3song.playmusic.ui.fragments.NowPlayingFragment;

public class NowPlayingActivity extends MusicServiceActivity {

    private static final String TAG = NowPlayingActivity.class.getSimpleName();
    private ActivityNowPlayingBinding nowPlayingBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nowPlayingBinding = DataBindingUtil.setContentView(NowPlayingActivity.this, R.layout.activity_now_playing);

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainLayout, NowPlayingFragment.newInstance());
        transaction.commit();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        nowPlayingBinding.appRoot.post(() -> handlePlaybackIntent(getIntent()));
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        if (mimeType == null) {
            mimeType = "";
        }
        boolean handled = false;

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            Log.d(TAG, "handlePlaybackIntent: type media play from search");
            handled = true;
        }

        if (uri != null && uri.toString().length() > 0) {
            Log.d(TAG, "handlePlaybackIntent: type play file");
            MusicPlayerRemote.playFromUri(uri);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            Log.d(TAG, "handlePlaybackIntent: type playlist");
            handled = true;
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            Log.d(TAG, "handlePlaybackIntent: type album");
            handled = true;
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            Log.d(TAG, "handlePlaybackIntent: type artist");
            handled = true;
        } else if (!handled && MusicService.ACTION_ON_CLICK_NOTIFICATION.equals(intent.getAction())) {
            handled = true;
        } else if (!handled) {
            Log.d(TAG, "handlePlaybackIntent: unhandled: " + intent.getAction());
        }

        if (handled) {
            setIntent(new Intent());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}