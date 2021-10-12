package com.mp3song.playmusic.contract;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.helpers.songpreview.PreviewSong;
import com.mp3song.playmusic.helpers.songpreview.SongPreviewController;
import com.mp3song.playmusic.helpers.songpreview.SongPreviewListener;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.musicglide.SongGlideRequest;
import com.mp3song.playmusic.service.MusicPlayerRemote;
import com.mp3song.playmusic.service.MusicService;
import com.mp3song.playmusic.ui.MusicServiceActivity;
import com.mp3song.playmusic.ui.adapter.Library.artist.ArtistAdapter;
import com.mp3song.playmusic.utils.CircularPlayPauseProgressBar;
import com.mp3song.playmusic.utils.Tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Handle Event:
 * <br>+. Click on item
 * <br>+. Long Click on item
 * <br>+. Click Preview Button on item
 * <br>+. Click Menu Button on item
 */

public abstract class AbsSongAdapter extends AbsMediaAdapter<AbsBindAbleHolder, Song> implements SongPreviewListener {
    private static final String TAG = "AbsSongAdapter";
    private PreviewSong mPreviewSong = null;

    public AbsSongAdapter() {
        super();
    }

    @Override
    public void init(Context context) {
        super.init(context);
        if (context instanceof MusicServiceActivity) {
            SongPreviewController controller = ((MusicServiceActivity) context).getSongPreviewController();
            if (controller != null)
                controller.addSongPreviewListener(this);
        }
    }

    @Override
    protected void onDataSet() {

        if (getContext() instanceof MainActivity) {
            SongPreviewController controller = ((MainActivity) getContext()).getSongPreviewController();
            if (controller != null)
                mPreviewSong = controller.getCurrentPreviewSong();
        }
    }

    public void destroy() {
        if (getContext() instanceof MainActivity) {
            SongPreviewController controller = ((MainActivity) getContext()).getSongPreviewController();
            if (controller != null) controller.removeAudioPreviewerListener(this);
        }
        super.destroy();
    }

    public void previewAll(boolean shuffle) {
        if (getContext() instanceof MainActivity) {
            SongPreviewController preview = ((MainActivity) getContext()).getSongPreviewController();
            if (preview != null) {
                if (preview.isPlayingPreview())
                    preview.cancelPreview();
                else {
                    if (shuffle) {
                        ArrayList<Song> list = new ArrayList<>(getData());
                        Collections.shuffle(list);
                        preview.previewSongs(list);
                    } else
                        preview.previewSongs(getData());
                }
            }
        }
    }

    protected void previewThisSong(int positionData) {

        if (getContext() instanceof MainActivity) {
            SongPreviewController preview = ((MainActivity) getContext()).getSongPreviewController();
            if (preview != null) {
                if (preview.isPlayingPreview() && preview.isPreviewingSong(getData().get(positionData)))
                    preview.cancelPreview();
                else {
                    preview.previewSongs(getData().get(positionData));
                }
            }
        }
    }

    private void forceStopPreview() {
        mPreviewSong = null;
        if (getContext() instanceof MainActivity) {
            SongPreviewController controller = ((MainActivity) getContext()).getSongPreviewController();
            if (controller != null) controller.cancelPreview();
        }
    }

    @Override
    public void onSongPreviewStart(PreviewSong song) {
        int position = getData().indexOf(song.getSong());

        Log.d(TAG, "onSongPreviewStart: position = " + position);
        if (position != -1) {
            mPreviewSong = song;
            notifyItemChanged(getMediaHolderPosition(position), SONG_PREVIEW_CHANGED);
        }
    }

    @Override
    public void onSongPreviewFinish(PreviewSong song) {
        mPreviewSong = null;
        int position = getData().indexOf(song.getSong());
        Log.d(TAG, "onSongPreviewFinish: position = " + position);

        if (position != -1)
            notifyItemChanged(getMediaHolderPosition(position), SONG_PREVIEW_CHANGED);
    }

    public void playAll(int startPosition, boolean startPlaying) {
        if (!getData().isEmpty()) {
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_NONE);
                MusicPlayerRemote.openQueue(getData(), startPosition, startPlaying);
                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    notifyItemChanged(getMediaHolderPosition(mMediaPlayDataItem), PLAY_STATE_CHANGED);
                    notifyItemChanged(getMediaHolderPosition(startPosition), PLAY_STATE_CHANGED);
                    mMediaPlayDataItem = startPosition;
                }, 50);
            }, 100);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbsBindAbleHolder holder, int position, @NonNull List<Object> payloads) {
        if (holder instanceof SongHolder && !payloads.isEmpty())
            for (Object object : payloads) {
                Log.d(TAG, "onBindViewHolder: object " + object);
                if (object instanceof String) {
                    switch ((String) object) {
                        case PLAY_STATE_CHANGED:
                            Log.d(TAG, mName + " onBindViewHolder: " + PLAY_STATE_CHANGED);
                            ((SongHolder) holder).bindMediaPlayState();
                            ((SongHolder) holder).bindTheme();
                            break;
                        case SONG_PREVIEW_CHANGED:
                            Log.d(TAG, mName + " onBindViewHolder: " + SONG_PREVIEW_CHANGED);
                            ((SongHolder) holder).bindPreviewButton(getDataItem(getDataPosition(position)));
                            break;
                        case PALETTE_CHANGED:
                            Log.d(TAG, mName + " onBindViewHolder: " + PALETTE_CHANGED);
                            ((SongHolder) holder).bindTheme();
                            break;
                        default:
                            super.onBindViewHolder(holder, position, payloads);
                    }
                } else super.onBindViewHolder(holder, position, payloads);
            }
        else {
            Log.d(TAG, mName + " onBindViewHolder: " + position + ", " + payloads);
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbsBindAbleHolder absBindAbleHolder, int i) {
        Log.d(TAG, "onBindViewHolder without payload " + i);
    }

    @Override
    public void notifyOnMediaStateChanged(final String whichChanged) {
        if (whichChanged.equals(PLAY_STATE_CHANGED)) {
            Song currentPlayingSong = MusicPlayerRemote.getCurrentSong();
            if (currentPlayingSong != null && currentPlayingSong.id != -1) {

                if (isMediaPlayItemAvailable()) {

                    notifyItemChanged(getMediaHolderPosition(mMediaPlayDataItem), PLAY_STATE_CHANGED);

                    boolean isStillOldPos = getData().get(mMediaPlayDataItem).equals(currentPlayingSong);
                    if (!isStillOldPos) {
                        mMediaPlayDataItem = getData().indexOf(currentPlayingSong);
                        if (mMediaPlayDataItem != -1)
                            notifyItemChanged(getMediaHolderPosition(mMediaPlayDataItem), PLAY_STATE_CHANGED);
                    }
                }
            }
        } else if (whichChanged.equals(PALETTE_CHANGED)) {
            notifyItemRangeChanged(0, getItemCount(), PALETTE_CHANGED);
        }
    }

    public class SongHolder extends AbsMediaHolder<Song> {

        @BindView(R.id.number)
        TextView mNumber;
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.description)
        TextView mDescription;

        @BindView(R.id.image)
        ImageView mImage;
        @BindView(R.id.quick_play_pause)
        ImageView mQuickPlayPause;
        @BindView(R.id.menu_button)
        View mMenuButton;

        @BindView(R.id.panel)
        View mPanel;

        @BindView(R.id.preview_button)
        CircularPlayPauseProgressBar mPreviewButton;

        public SongHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.menu_button)
        void clickMenu() {
            onMenuItemClick(getDataPosition(getAdapterPosition()));
        }

        @Override
        public void onClick(View view) {
            playAll(getDataPosition(getAdapterPosition()), true);
        }

        private void resetProgress() {
            mPreviewButton.resetProgress();
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(Song song) {
            Log.d(TAG, "bind");
            mNumber.setText("" + (getDataPosition(getAdapterPosition()) + 1));
            mTitle.setText(song.title);
            mDescription.setText(song.artistName);

            SongGlideRequest.Builder.from(Glide.with(getContext()), song)
                    .ignoreMediaStore(false)
                    .generatePalette(getContext()).build()
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            if (mImage instanceof RoundedImageView)
                                ((RoundedImageView) mImage).setBorderWidth(R.dimen.oneDP);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            if (mImage instanceof RoundedImageView)
                                ((RoundedImageView) mImage).setBorderWidth(0f);
                            return false;
                        }
                    })
                    .into(mImage);

            bindTheme();

            bindPreviewButton(song);
        }

        void bindTheme() {
            bindMediaPlayState();
        }

        private void bindPreviewButton(Song song) {
            if ((mPreviewSong == null || !mPreviewSong.getSong().equals(song))
                    && mPreviewButton.getMode() == CircularPlayPauseProgressBar.PLAYING)
                mPreviewButton.resetProgress();
            else if (mPreviewSong != null && mPreviewSong.getSong().equals(song)) {
                long timePlayed = mPreviewSong.getTimePlayed();
                //    Log.d(TAG, "bindPreviewButton: timePlayed = " + timePlayed);
                if (timePlayed == -1) mPreviewButton.resetProgress();
                else {
                    if (timePlayed < 0) timePlayed = 0;
                    if (timePlayed <= mPreviewSong.getTotalPreviewDuration())
                        mPreviewButton.syncProgress(mPreviewSong.getTotalPreviewDuration(), (int) timePlayed);
                }
            }
        }

        private void bindMediaPlayState() {
            if (MusicPlayerRemote.getCurrentSong().id == getData().get(getDataPosition(getAdapterPosition())).id) {
                mMediaPlayDataItem = getDataPosition(getAdapterPosition());
                int baseColor = ArtistAdapter.lighter(Tool.getBaseColor(), 0.6f);
                mTitle.setTextColor(getContext().getResources().getColor(R.color.white));
                mDescription.setTextColor(getContext().getResources().getColor(R.color.white));
                mPanel.setBackground(getContext().getResources().getDrawable(R.drawable.cancel_btn));
//                mQuickPlayPause.setColorFilter(getContext().getResources().getColor(R.color.current_color));

                if (MusicPlayerRemote.isPlaying()) {
                    mQuickPlayPause.setImageResource(R.drawable.ic_pause);
                } else {
                    mQuickPlayPause.setImageResource(R.drawable.ic_play);
                }
            } else {
                mQuickPlayPause.setImageDrawable(null);
                int flatWhite = mQuickPlayPause.getResources().getColor(R.color.text_color_back);
                mTitle.setTextColor(mQuickPlayPause.getResources().getColor(R.color.text_color_back));
                mDescription.setTextColor(Color.argb(0xAA, Color.red(flatWhite), Color.green(flatWhite), Color.blue(flatWhite)));
                mPanel.setBackground(null);
            }
        }

        @OnClick(R.id.preview_button)
        void clickPresent() {

            if (mPreviewSong != null && mPreviewSong.getSong().equals(getData().get(getDataPosition(getAdapterPosition())))) {
                resetProgress();
                forceStopPreview();
            } else {
                previewThisSong(getDataPosition(getAdapterPosition()));
            }
        }
    }

}
