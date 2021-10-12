package com.mp3song.playmusic.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.NowPlayingActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.databinding.FragmentNowPlayingLayerBinding;
import com.mp3song.playmusic.helpers.avsbb.AudioVisualSeekBar;
import com.mp3song.playmusic.medialoader.ArtistLoader;
import com.mp3song.playmusic.medialoader.SongLoader;
import com.mp3song.playmusic.models.Artist;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.service.MusicPlayerRemote;
import com.mp3song.playmusic.service.MusicServiceEventListener;
import com.mp3song.playmusic.ui.CardLayerController;
import com.mp3song.playmusic.ui.MusicServiceActivity;
import com.mp3song.playmusic.ui.adapter.NowPlayingAdapter;
import com.mp3song.playmusic.ui.pages.CardLayerFragment;
import com.mp3song.playmusic.utils.SortOrder;
import com.mp3song.playmusic.utils.Tool;
import com.mp3song.playmusic.utils.Util;
import com.mp3song.playmusic.utils.imageload.ArtistGlideRequest;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NowPlayingLayerFragment extends CardLayerFragment implements MusicServiceEventListener, AudioVisualSeekBar.OnSeekBarChangeListener {

    public static final int WHAT_CARD_LAYER_HEIGHT_CHANGED = 101;
    public static final int WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION = 102;
    public static final int WHAT_UPDATE_CARD_LAYER_RADIUS = 103;

    private static final String TAG = "NowPlayingLayerFragment";
    private final NowPlayingAdapter mAdapter = new NowPlayingAdapter();
    public FragmentNowPlayingLayerBinding nowPlayingLayerBinding;
    SnapHelper snapHelper = new PagerSnapHelper();
    boolean fragmentPaused = false;
    private float mMaxRadius = 18;
    private boolean isTouchedVisualSeekbar = false;
    private int overflowcounter = 0;
    private boolean mTimeTextIsSync = false;
    public Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            long position = MusicPlayerRemote.getSongProgressMillis();
            if (!isTouchedVisualSeekbar)
                setTextTime(position, MusicPlayerRemote.getSongDurationMillis());

            nowPlayingLayerBinding.visualSeekBar.setProgress((int) position);
            overflowcounter--;
            if (MusicPlayerRemote.isPlaying()) {
                int delay = (int) (150 - (position) % 100);
                if (overflowcounter < 0 && !fragmentPaused) {
                    overflowcounter++;
                    nowPlayingLayerBinding.visualSeekBar.postDelayed(mUpdateProgress, delay);
                }
            }
        }
    };
    private final String timeTextViewTemp = "00:00";
    private final String timeTextViewTemp1 = "00:00";
    private final Handler mNowPlayingHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            if (isAdded() && !isDetached() && !isRemoving()) {
                switch (what) {
                    case WHAT_CARD_LAYER_HEIGHT_CHANGED:
                        handleLayerHeightChanged();
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

    // TODO: Rename parameter arguments, choose names that match

    public NowPlayingLayerFragment() {
        // Required empty public constructor
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

        nowPlayingLayerBinding.recyclerView.setOnTouchListener((v, event) -> mCardLayerController.dispatchOnTouchEvent(nowPlayingLayerBinding.root, event));
        nowPlayingLayerBinding.visualSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mCardLayerController.dispatchOnTouchEvent(nowPlayingLayerBinding.root, event) && event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_UP;
            }
        });

        nowPlayingLayerBinding.visualSeekBar.setOnSeekBarChangeListener(this);
        Log.d(TAG, "onViewCreated");
        if (getActivity() instanceof MusicServiceActivity) {
            ((MainActivity) getActivity()).addMusicServiceEventListener(this, true);
        }
        new Handler(Looper.getMainLooper()).post(this::setUp);

        nowPlayingLayerBinding.imgBack.setOnClickListener(v -> {
            CardLayerController.CardLayerAttribute a = getCardLayerController().getMyAttr(NowPlayingLayerFragment.this);
            if (a != null) {
                if (a.getState() == CardLayerController.CardLayerAttribute.MINIMIZED)
                    a.animateToMax();
                else
                    a.animateToMin();
            }
        });

        nowPlayingLayerBinding.minimizeBar.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), NowPlayingActivity.class);
            startActivity(intent);
        });

        nowPlayingLayerBinding.prevButton1.setOnClickListener(v -> MusicPlayerRemote.back());

        nowPlayingLayerBinding.nextButton1.setOnClickListener(v -> MusicPlayerRemote.playNextSong());

        nowPlayingLayerBinding.buttonRight.setOnClickListener(v -> MusicPlayerRemote.playOrPause());
    }

    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container) {
        nowPlayingLayerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_now_playing_layer, container, false);

        return nowPlayingLayerBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        if (getActivity() instanceof MusicServiceActivity) {
            ((MainActivity) getActivity()).removeMusicServiceEventListener(this);
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
        if (nowPlayingLayerBinding.root != null) {
            float valueTemp;
            if (value > 1) valueTemp = 1;
            else if (value <= 0.1f) valueTemp = 0;
            else valueTemp = value;
            nowPlayingLayerBinding.root.setRadius(mMaxRadius * valueTemp);
        }
    }

    @Override
    public void onLayerUpdate(ArrayList<CardLayerController.CardLayerAttribute> attrs, ArrayList<Integer> actives, int me) {
        if (isAdded() && !isRemoving() && !isDetached()) {
            float translationPercent = attrs.get(actives.get(0)).getRuntimePercent();
            if (me == 1) {
                nowPlayingLayerBinding.dimView.setAlpha(0.3f * translationPercent);
                setCardRadius(translationPercent);
            } else {
                float min = 0.3f, max = 0.65f;
                float hieu = max - min;
                float heSo_sau = (me - 1.0f) / (me - 0.75f); // 1/2, 2/3,3/4, 4/5, 5/6 ...
                float heSo_truoc = (me - 2.0f) / (me - 0.75f); // 0/1, 1/2, 2/3, ...
                float darken = min + hieu * heSo_truoc + hieu * (heSo_sau - heSo_truoc) * translationPercent;
                nowPlayingLayerBinding.dimView.setAlpha(darken);
                setCardRadius(1);
            }
        }
        //  checkStatusStyle();
    }

    @Override
    public void onLayerHeightChanged(CardLayerController.CardLayerAttribute attr) {
        handleLayerHeightChanged();
    }

    private void handleUpdateCardLayerRadius() {
        if (isFullscreenLayer()) {
            setCardRadius(0);
        } else {
            setCardRadius(nowPlayingLayerBinding.constraintRoot.getProgress());
        }
    }

    private void handleLayerHeightChanged() {
        if (isAdded() && !isRemoving() && !isDetached()) {
            CardLayerController.CardLayerAttribute attribute = NowPlayingLayerFragment.this.getCardLayerController().getMyAttr(NowPlayingLayerFragment.this);
            if (attribute != null && isAdded() && !isRemoving() && !isDetached()) {
                float progress = attribute.getRuntimePercent();

                nowPlayingLayerBinding.constraintRoot.setProgress(progress);
                // sync time text view
                if (progress != 0 && !mTimeTextIsSync) {
                    nowPlayingLayerBinding.tvElipsedTime.setText(timeTextViewTemp);
                    nowPlayingLayerBinding.tvTotalTime.setText(timeTextViewTemp1);
                }

                sendMessage(WHAT_UPDATE_CARD_LAYER_RADIUS);
                sendMessage(WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION);
            }
        }
    }

    private void handleRecyclerViewScrollToCurrentPosition() {
        if (nowPlayingLayerBinding.constraintRoot.getProgress() == 0 || nowPlayingLayerBinding.constraintRoot.getProgress() == 1) {
            try {
                int position = MusicPlayerRemote.getPosition();
                if (position >= 0) {
                    nowPlayingLayerBinding.recyclerView.scrollToPosition(position);
                }
                //mViewPager.setCurrentItem(MusicPlayerRemote.getPosition());
            } catch (Exception ignore) {
            }
        }
    }

    private void postRunnable(Runnable runnable) {
        mNowPlayingHandler.removeCallbacks(runnable);
        mNowPlayingHandler.post(runnable);
    }

    private void sendMessage(int what) {
        mNowPlayingHandler.removeMessages(what);
        mNowPlayingHandler.sendEmptyMessage(what);
    }

    private void sendMessage(Message m) {
        mNowPlayingHandler.removeMessages(m.what);
        mNowPlayingHandler.sendMessage(m);
    }

    public static int isEdgeToEdgeEnabled(Context context) {
        Resources resources = context.getResources();
//        int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int isEdgeToEdgeEnabled1(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
//        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getInteger(resourceId);
        }
        return 0;
    }

    public boolean hasNavBar(Activity activity) {
        Point realSize = new Point();
        Point screenSize = new Point();
        boolean hasNavBar = false;
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        realSize.x = metrics.widthPixels;
        realSize.y = metrics.heightPixels;
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
        if (realSize.y != screenSize.y) {
            int difference = realSize.y - screenSize.y;
            int navBarHeight = 0;
            Resources resources = activity.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navBarHeight = resources.getDimensionPixelSize(resourceId);
            }
            if (navBarHeight != 0) {
                if (difference == navBarHeight) {
                    hasNavBar = true;
                }
            }

        }
        return hasNavBar;

    }

    @Override
    public int getLayerMinHeight(Context context, int h) {
        int systemInsetsBottom = 0;
        CardLayerController controller = getCardLayerController();
        Activity activity = controller != null ? controller.getActivity() : getActivity();
        if (activity instanceof MainActivity) {
            systemInsetsBottom = ((MainActivity) activity).getCurrentSystemInsets()[3];
        }
        Log.d(TAG, "activity availibility = " + (activity != null));
        Log.d(TAG, "systemInsetsBottom = " + systemInsetsBottom);
        Log.e("LLL_Gesture: ", String.valueOf(isEdgeToEdgeEnabled(context)));
        Log.e("LLL_Gesture11: ", String.valueOf(hasNavBar(activity)));
        if (isEdgeToEdgeEnabled1(context) == 3) {
            return 130 + (int) (50 + context.getResources().getDimension(R.dimen.now_laying_height_in_minimize_mode));
        }
        return isEdgeToEdgeEnabled(context) + 50;
    }

    @Override
    public String getCardLayerTag() {
        return TAG;
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
    public boolean onGestureDetected(int gesture) {
        if (gesture == CardLayerController.SINGLE_TAP_UP) {
            CardLayerController.CardLayerAttribute a = getCardLayerController().getMyAttr(this);
            if (a != null) {
                a.animateToMin();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFullscreenLayer() {
        return true;
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
        requireActivity().runOnUiThread(() -> setTextTime(seekingValue, MusicPlayerRemote.getSongDurationMillis()));
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

        String finalText = text;
        String finalText1 = text1;

        requireActivity().runOnUiThread(() -> {
            nowPlayingLayerBinding.tvElipsedTime.setText(finalText);
            nowPlayingLayerBinding.tvTotalTime.setText(finalText1);
            mTimeTextIsSync = true;
        });
    }
}