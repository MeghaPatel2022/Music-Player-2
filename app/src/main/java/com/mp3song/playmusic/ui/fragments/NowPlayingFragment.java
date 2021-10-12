package com.mp3song.playmusic.ui.fragments;

import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.mp3song.playmusic.NowPlayingActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.databinding.FragmentNowPlayingExpandedBinding;
import com.mp3song.playmusic.helpers.avsbb.AudioVisualSeekBar;
import com.mp3song.playmusic.medialoader.ArtistLoader;
import com.mp3song.playmusic.medialoader.SongLoader;
import com.mp3song.playmusic.models.Artist;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.service.MusicPlayerRemote;
import com.mp3song.playmusic.service.MusicService;
import com.mp3song.playmusic.service.MusicServiceEventListener;
import com.mp3song.playmusic.ui.MusicServiceActivity;
import com.mp3song.playmusic.ui.adapter.NowPlayingAdapter;
import com.mp3song.playmusic.utils.SortOrder;
import com.mp3song.playmusic.utils.Tool;
import com.mp3song.playmusic.utils.Util;
import com.mp3song.playmusic.utils.imageload.ArtistGlideRequest;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class NowPlayingFragment extends Fragment implements MusicServiceEventListener, AudioVisualSeekBar.OnSeekBarChangeListener {

    public static final int WHAT_CARD_LAYER_HEIGHT_CHANGED = 101;
    public static final int WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION = 102;
    public static final int WHAT_UPDATE_CARD_LAYER_RADIUS = 103;

    private static final String TAG = "NowPlayingFragmentEx";
    private final NowPlayingAdapter mAdapter = new NowPlayingAdapter();
    public FragmentNowPlayingExpandedBinding nowPlayingLayerBinding;
    SnapHelper snapHelper = new PagerSnapHelper();
    boolean fragmentPaused = false;
    private float mMaxRadius = 18;
    private final Handler mNowPlayingHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            if (isAdded() && !isDetached() && !isRemoving()) {
                switch (what) {
                    case WHAT_CARD_LAYER_HEIGHT_CHANGED:
                        break;
                    case WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION:
                        handleRecyclerViewScrollToCurrentPosition();
                        break;
                    case WHAT_UPDATE_CARD_LAYER_RADIUS:
                        handleUpdateCardLayerRadius();
                        break;
                }
            }
        }
    };
    private boolean isTouchedVisualSeekbar = false;
    private int overflowcounter = 0;
    public Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            long position = MusicPlayerRemote.getSongProgressMillis();

            if (!isTouchedVisualSeekbar)
                setTextTime(position, MusicPlayerRemote.getSongDurationMillis());

            nowPlayingLayerBinding.visualSeekBar.setProgress((int) position);
            //TODO: Set elapsedTime
            overflowcounter--;
            if (MusicPlayerRemote.isPlaying()) {
                //TODO: ???
                int delay = (int) (150 - (position) % 100);
                if (overflowcounter < 0 && !fragmentPaused) {
                    overflowcounter++;
                    nowPlayingLayerBinding.visualSeekBar.postDelayed(mUpdateProgress, delay);
                }
            }
        }
    };

    public NowPlayingFragment() {
        // Required empty public constructor
    }

    // TODO: Rename parameter arguments, choose names that match

    public static NowPlayingFragment newInstance() {
        NowPlayingFragment fragment = new NowPlayingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMaxRadius = requireContext().getResources().getDimension(R.dimen.max_radius_layer);
        nowPlayingLayerBinding.title.setSelected(true);

        nowPlayingLayerBinding.recyclerView.setAdapter(mAdapter);
        nowPlayingLayerBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        ViewCompat.setOnApplyWindowInsetsListener(nowPlayingLayerBinding.safeViewTop, (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), 0);
            v.requestLayout();
            return ViewCompat.onApplyWindowInsets(v, insets);
        });

        ViewCompat.setOnApplyWindowInsetsListener(nowPlayingLayerBinding.safeViewBottom, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                v.setPadding(insets.getSystemWindowInsetLeft(), 0, insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                v.requestLayout();
                return ViewCompat.onApplyWindowInsets(v, insets);
            }
        });

        snapHelper.attachToRecyclerView(nowPlayingLayerBinding.recyclerView);
        nowPlayingLayerBinding.visualSeekBar.setOnSeekBarChangeListener(this);
        Log.d(TAG, "onViewCreated");
        if (getActivity() instanceof MusicServiceActivity) {
            ((NowPlayingActivity) getActivity()).addMusicServiceEventListener(this, true);
        }
        new Handler(Looper.getMainLooper()).post(this::setUp);

        nowPlayingLayerBinding.imgBack.setOnClickListener(v -> requireActivity().onBackPressed());

        setShuffle();
        setRepeat();

        nowPlayingLayerBinding.imgShuffle.setOnClickListener(v -> {
            if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
                Toasty.info(requireContext(), "Shuffle Off", Toasty.LENGTH_SHORT).show();
                nowPlayingLayerBinding.imgShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle));
                MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_NONE);
            } else {
                Toasty.info(requireContext(), "Shuffle On", Toasty.LENGTH_SHORT).show();
                nowPlayingLayerBinding.imgShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle_on));
                nowPlayingLayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat);
                MusicPlayerRemote.setShuffleMode(MusicService.REPEAT_MODE_NONE);
                MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE);
            }
        });

        nowPlayingLayerBinding.imgRepeat.setOnClickListener(v -> {
            MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_NONE);
            MusicPlayerRemote.cycleRepeatMode();
            setShuffle();
            int mode = MusicPlayerRemote.getRepeatMode();

            switch (mode) {
                case MusicService.REPEAT_MODE_NONE:
                    Log.d(TAG, "updateRepeatState: None");
                    nowPlayingLayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat);
                    Toasty.info(requireContext(), "Repeat Off", Toasty.LENGTH_SHORT).show();
                    break;
                case MusicService.REPEAT_MODE_THIS:
                    Log.d(TAG, "updateRepeatState: Current");
                    nowPlayingLayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat_one);
                    Toasty.info(requireContext(), "Repeat One", Toasty.LENGTH_SHORT).show();
                    break;
                case MusicService.REPEAT_MODE_ALL:
                    Log.d(TAG, "updateRepeatState: All");
                    nowPlayingLayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat_all);
                    Toasty.info(requireContext(), "Repeat All", Toasty.LENGTH_SHORT).show();
                    break;
            }
        });

        nowPlayingLayerBinding.prevButton.setOnClickListener(v -> MusicPlayerRemote.back());
        nowPlayingLayerBinding.nextButton.setOnClickListener(v -> MusicPlayerRemote.playNextSong());
        nowPlayingLayerBinding.playPauseButton.setOnClickListener(v -> MusicPlayerRemote.playOrPause());
    }

    private void setShuffle() {
        if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
            nowPlayingLayerBinding.imgShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle_on));
        } else {
            nowPlayingLayerBinding.imgShuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle));
        }
    }

    private void setRepeat() {
        int mode = MusicPlayerRemote.getRepeatMode();

        switch (mode) {
            case MusicService.REPEAT_MODE_NONE:
                Log.d(TAG, "updateRepeatState: None");
                nowPlayingLayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat);
                break;
            case MusicService.REPEAT_MODE_THIS:
                Log.d(TAG, "updateRepeatState: Current");
                nowPlayingLayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat_one);
                break;
            case MusicService.REPEAT_MODE_ALL:
                nowPlayingLayerBinding.imgRepeat.setImageResource(R.drawable.ic_repeat_all);
                Log.d(TAG, "updateRepeatState: All");
        }
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        nowPlayingLayerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_now_playing_expanded, container, false);

        return nowPlayingLayerBinding.getRoot();
    }

    @Override
    public void onDestroyView() {

        if (getActivity() instanceof MusicServiceActivity) {
            ((NowPlayingActivity) getActivity()).removeMusicServiceEventListener(this);
        }
        super.onDestroyView();
    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        setUp();
    }

    @Override
    public void onServiceDisconnected() {
        Log.d(TAG, "onServiceDisconnected");
    }

    @Override
    public void onQueueChanged() {
        Log.d(TAG, "onQueueChanged");
        updateQueue();
    }

    @Override
    public void onPlayingMetaChanged() {
        Log.d(TAG, "onPlayingMetaChanged");
        updatePlayingSongInfo();
        sendMessage(WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION);
    }

    @Override
    public void onPlayStateChanged() {
        Log.d(TAG, "onPlayStateChanged");
        updatePlayPauseState();
        nowPlayingLayerBinding.visualSeekBar.postDelayed(mUpdateProgress, 10);
    }

    @Override
    public void onRepeatModeChanged() {
        Log.d(TAG, "onRepeatModeChanged");
    }

    @Override
    public void onShuffleModeChanged() {
        Log.d(TAG, "onShuffleModeChanged");
    }

    @Override
    public void onMediaStoreChanged() {
        Log.d(TAG, "onMediaStoreChanged");
        updateQueue();
    }

    @Override
    public void onPaletteChanged() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            ((RippleDrawable) nowPlayingLayerBinding.prevButton.getBackground()).setColor(ColorStateList.valueOf(Tool.getBaseColor()));
            ((RippleDrawable) nowPlayingLayerBinding.nextButton.getBackground()).setColor(ColorStateList.valueOf(Tool.getBaseColor()));
            ((RippleDrawable) nowPlayingLayerBinding.playlistTitle.getBackground()).setColor(ColorStateList.valueOf(Tool.getBaseColor()));
        }
        onColorPaletteReady(Tool.ColorOne, Tool.ColorTwo, Tool.AlphaOne, Tool.AlphaTwo);
    }

    private void updateQueue() {
        mAdapter.setData(MusicPlayerRemote.getPlayingQueue());
    }

    public void setUp() {
        if (getView() != null && isAdded() && !isRemoving() && !isDetached()) {
            updatePlayingSongInfo();
            updatePlayPauseState();
            updateQueue();
            sendMessage(WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION);
        }
    }

    private void setCardRadius(float value) {
        float valueTemp;
        if (value > 1) valueTemp = 1;
        else if (value <= 0.1f) valueTemp = 0;
        else valueTemp = value;
        nowPlayingLayerBinding.root.setRadius(mMaxRadius * valueTemp);
    }

    private void handleUpdateCardLayerRadius() {
        setCardRadius(nowPlayingLayerBinding.constraintRoot.getProgress());
    }

    private void handleRecyclerViewScrollToCurrentPosition() {
        if (nowPlayingLayerBinding.constraintRoot.getProgress() == 0 || nowPlayingLayerBinding.constraintRoot.getProgress() == 1) {
            try {
                int position = MusicPlayerRemote.getPosition();
                if (position >= 0) {
                    nowPlayingLayerBinding.recyclerView.scrollToPosition(position);
                }
            } catch (Exception ignore) {
            }
        }
    }

    private void sendMessage(int what) {
        mNowPlayingHandler.removeMessages(what);
        mNowPlayingHandler.sendEmptyMessage(what);
    }

    void updatePlayPauseState() {
        if (MusicPlayerRemote.isPlaying()) {
            nowPlayingLayerBinding.buttonRight.setImageResource(R.drawable.ic_pause);
            nowPlayingLayerBinding.playPauseButton.setImageResource(R.drawable.ic_pause);
        } else {
            nowPlayingLayerBinding.buttonRight.setImageResource(R.drawable.ic_play);
            nowPlayingLayerBinding.playPauseButton.setImageResource(R.drawable.ic_play);
        }
    }

    private void updatePlayingSongInfo() {
        Song song = MusicPlayerRemote.getCurrentSong();
        if (song == null || song.id == -1) {
            ArrayList<Song> list = SongLoader.getAllSongs(nowPlayingLayerBinding.playPauseButton.getContext(), SortOrder.SongSortOrder.SONG_DATE);
            if (list.isEmpty()) return;
            MusicPlayerRemote.openQueue(list, 0, false);
            return;
        }

        nowPlayingLayerBinding.title.setText(String.format("%s", song.title));
        nowPlayingLayerBinding.mAlbum.setText(String.format("%s", song.artistName));
        nowPlayingLayerBinding.bigTitle.setText(song.title);
        nowPlayingLayerBinding.bigArtist.setText(song.artistName);

        nowPlayingLayerBinding.title.setText(String.format("%s", song.title));
        nowPlayingLayerBinding.mAlbum.setText(String.format("%s", song.artistName));
        nowPlayingLayerBinding.bigTitle.setText(song.title);
        nowPlayingLayerBinding.bigArtist.setText(song.artistName);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nowPlayingLayerBinding.image.setClipToOutline(true);
        }

        Artist artist = ArtistLoader.getArtist(requireContext(), song.artistId);
        int[] screen = Tool.getScreenSize(requireContext());

        Glide.with(getContext())
                .load(Util.getAlbumArtUri(song.albumId))
                .override(screen[1])
                .placeholder(R.drawable.ic_default_album_art)
                .error(
                        ArtistGlideRequest.Builder.from(Glide.with(getContext()), artist).tryToLoadOriginal(true).whichImage(1).generateBuilder(getContext()).buildRequestDrawable()
                                .error(ArtistGlideRequest.Builder.from(Glide.with(getContext()), artist).tryToLoadOriginal(false).whichImage(1).generateBuilder(getContext()).buildRequestDrawable().error(R.drawable.ic_default_album_art)
                                ))
                .into(nowPlayingLayerBinding.image);

        String path = song.data;
        long duration = song.duration;
        Log.d(TAG, "start visualize " + path + "dur = " + duration + ", pos = " + MusicPlayerRemote.getSongProgressMillis());
        Log.d(TAG, "Seek: " + nowPlayingLayerBinding.visualSeekBar.getCurrentSongID() + " SongID: " + song.id);
        if (duration > 0 && path != null && !path.isEmpty() && nowPlayingLayerBinding.visualSeekBar.getCurrentSongID() != song.id) {
            nowPlayingLayerBinding.visualSeekBar.visualize(song, duration, MusicPlayerRemote.getSongProgressMillis());
        } else {
            Log.d(TAG, "ignore visualize " + path);
        }

        nowPlayingLayerBinding.visualSeekBar.postDelayed(mUpdateProgress, 10);
        if (getActivity() instanceof MusicServiceActivity)
            ((MusicServiceActivity) getActivity()).refreshPalette();
    }

    private void onColorPaletteReady(int color1, int color2, float alpha1, float alpha2) {
        Log.d(TAG, "onColorPaletteReady :" + color1 + ", " + color2 + ", " + alpha1 + ", " + alpha2);
        nowPlayingLayerBinding.bigTitle.setTextColor(Tool.lighter(color1, 0.5f));
        nowPlayingLayerBinding.bigArtist.setTextColor(color2);
        nowPlayingLayerBinding.visualSeekBar.updateDrawProperties();
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        fragmentPaused = false;
        nowPlayingLayerBinding.visualSeekBar.postDelayed(mUpdateProgress, 10);
    }

    @Override
    public void onSeekBarSeekTo(AudioVisualSeekBar seekBar, int position) {
        MusicPlayerRemote.seekTo(position);
    }

    @Override
    public void onSeekBarTouchDown(AudioVisualSeekBar seekBar) {
        isTouchedVisualSeekbar = true;
    }

    @Override
    public void onSeekBarTouchUp(AudioVisualSeekBar seekBar) {
        isTouchedVisualSeekbar = false;
    }

    @Override
    public void onSeekBarSeeking(int seekingValue) {
        setTextTime(seekingValue, MusicPlayerRemote.getSongDurationMillis());
    }

    private void setTextTime(long pos, long duration) {
        int minute = (int) (pos / 1000 / 60);
        int second = (int) (pos / 1000 - minute * 60);
        int dur_minute = (int) (duration / 1000 / 60);
        int dur_second = (int) (duration / 1000 - dur_minute * 60);

        String text = "";
        String text1 = "";
        if (minute < 10) text += "0";
        text += minute + ":";
        if (second < 10) text += "0";
        text += second;
        if (dur_minute < 10) text1 += "0";
        text1 += dur_minute + ":";
        if (dur_second < 10) text1 += "0";
        text1 += dur_second;

        nowPlayingLayerBinding.tvElipsedTime.setText(text);
        nowPlayingLayerBinding.tvTotalTime.setText(text1);
    }
}