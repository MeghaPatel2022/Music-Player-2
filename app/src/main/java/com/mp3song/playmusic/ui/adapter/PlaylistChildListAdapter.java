package com.mp3song.playmusic.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.databinding.ListItemPlaylistBinding;
import com.mp3song.playmusic.medialoader.LastAddedLoader;
import com.mp3song.playmusic.medialoader.PlaylistSongLoader;
import com.mp3song.playmusic.medialoader.TopAndRecentlyPlayedTracksLoader;
import com.mp3song.playmusic.models.Playlist;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.utils.AutoGeneratedPlaylistBitmap;
import com.mp3song.playmusic.utils.Tool;
import com.mp3song.playmusic.utils.Util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PlaylistChildListAdapter extends RecyclerView.Adapter<PlaylistChildListAdapter.ItemHolder> {
    private static final String TAG = "PlaylistAdapter";
    public ArrayList<Playlist> mPlaylistData = new ArrayList<>();
    public PlaylistClickListener mListener;
    private Context mContext;
    private boolean showAuto;
    private int songCountInt;
    private long firstAlbumID = -1;

    public void init(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public void setShowAuto(boolean showAuto) {
        this.showAuto = showAuto;
    }

    public void setOnItemClickListener(PlaylistClickListener listener) {
        mListener = listener;
    }

    public void unBindAdapter() {
        mListener = null;
        mContext = null;
    }

    public void setData(List<Playlist> data) {
        Log.d(TAG, "setData: count = " + data.size());
        mPlaylistData.clear();
        if (data != null) {
            mPlaylistData.addAll(data);
            notifyDataSetChanged();

        }
    }

    public void addData(ArrayList<Playlist> data) {
        if (data != null) {
            int posBefore = mPlaylistData.size();
            mPlaylistData.addAll(data);
            notifyItemRangeInserted(posBefore, data.size());
        }
    }

    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        ListItemPlaylistBinding playlistBinding =
                DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.list_item_playlist, viewGroup, false);
        return new ItemHolder(playlistBinding);
    }

    @Override
    public void onBindViewHolder(@NotNull final ItemHolder itemHolder, int i) {
        final Playlist playlist = mPlaylistData.get(i);

        Log.d(TAG, "one");
        if (!playlist.name.equals(getContext().getResources().getString(R.string.action_new_playlist))) {
            new PlaylistBitmapLoader(this, playlist, itemHolder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Glide.with(getContext())
                    .load(R.drawable.ic_add_playlist)
                    .error(R.drawable.ic_default_album_art)
                    .placeholder(R.drawable.ic_default_album_art)
                    .into(itemHolder.playlistBinding.image);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            itemHolder.playlistBinding.image.setClipToOutline(true);
        }

        itemHolder.playlistBinding.image.setTag(firstAlbumID);
        itemHolder.playlistBinding.title.setText(playlist.name);
        if (Util.isLollipop())
            itemHolder.playlistBinding.image.setTransitionName("transition_album_art" + i);
    }

    @Override
    public int getItemCount() {
        return mPlaylistData.size();
    }

    public List<Song> getPlaylistWithListId(int position, int id) {
        if (mContext != null) {
            firstAlbumID = -1;
            if (showAuto) {
                switch (position) {
                    case 0:
                        return LastAddedLoader.getLastAddedSongs(mContext);
                    case 1:
                        return TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mContext);
                    case 2:
                        return TopAndRecentlyPlayedTracksLoader.getTopTracks(mContext);
                    default:
                        return new ArrayList<>(PlaylistSongLoader.getPlaylistSongList(mContext, id));
                }
            } else PlaylistSongLoader.getPlaylistSongList(mContext, id);
        }
        return null;
    }

    public interface PlaylistClickListener {
        void onClickPlaylist(Playlist playlist, @Nullable Bitmap bitmap);
    }

    private static class PlaylistBitmapLoader extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<PlaylistChildListAdapter> mWeakAdapter;
        private final WeakReference<ItemHolder> mWeakItemHolder;
        private final Playlist mPlaylist;

        PlaylistBitmapLoader(PlaylistChildListAdapter adapter, Playlist playlist, ItemHolder item) {
            mWeakAdapter = new WeakReference<>(adapter);
            mWeakItemHolder = new WeakReference<>(item);
            mPlaylist = playlist;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ItemHolder itemHolder = mWeakItemHolder.get();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemHolder.playlistBinding.image.setClipToOutline(true);
            }
            PlaylistChildListAdapter adapter = mWeakAdapter.get();
            if (itemHolder != null) {
                if (!mPlaylist.name.equals(adapter.getContext().getResources().getString(R.string.action_new_playlist))) {
                    Glide.with(adapter.getContext())
                            .load(bitmap)
                            .error(R.drawable.ic_default_album_art)
                            .placeholder(R.drawable.ic_default_album_art)
                            .into(itemHolder.playlistBinding.image);
                } else {
                    Glide.with(adapter.getContext())
                            .load(R.drawable.ic_add_playlist)
                            .error(R.drawable.ic_default_album_art)
                            .placeholder(R.drawable.ic_default_album_art)
                            .into(itemHolder.playlistBinding.image);
                }
            }
        }

        @Override
        protected Bitmap doInBackground(Void... v) {
            PlaylistChildListAdapter adapter = mWeakAdapter.get();
            ItemHolder itemHolder = mWeakItemHolder.get();
            if (adapter != null && itemHolder != null) {
                List<Song> l = adapter.getPlaylistWithListId(itemHolder.getAdapterPosition(), mPlaylist.id);
                if (l.size() > 0)
                    return AutoGeneratedPlaylistBitmap.getBitmap(adapter.getContext(), l, false, false);
                else
                    return null;
            } else return null;
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {

        private final ListItemPlaylistBinding playlistBinding;

        int currentColor = 0;

        ItemHolder(ListItemPlaylistBinding playlistBinding) {
            super(playlistBinding.getRoot());
            this.playlistBinding = playlistBinding;
            playlistBinding.getRoot().setOnClickListener(this);
            playlistBinding.playlistOver.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            //Todo: Navigate to playlist detail

            if (mListener != null) {
                Bitmap bitmap = null;
                Drawable d = playlistBinding.image.getDrawable();
                if (d instanceof BitmapDrawable) bitmap = ((BitmapDrawable) d).getBitmap();
                mListener.onClickPlaylist(mPlaylistData.get(getAdapterPosition()), bitmap);
            }
            //itemView.startAnimation(myAnim);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (currentColor != Tool.getBaseColor()) {
                currentColor = Tool.getBaseColor();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((RippleDrawable) playlistBinding.playlistOver.getBackground()).setColor(ColorStateList.valueOf(Tool.getBaseColor()));
                }
            }
            return false;
        }
    }

}
