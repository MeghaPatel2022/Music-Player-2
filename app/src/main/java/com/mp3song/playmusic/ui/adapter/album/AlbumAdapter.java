package com.mp3song.playmusic.ui.adapter.album;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.contract.AbsBindAbleHolder;
import com.mp3song.playmusic.contract.AbsMediaAdapter;
import com.mp3song.playmusic.medialoader.GenreLoader;
import com.mp3song.playmusic.models.Album;
import com.mp3song.playmusic.models.Genre;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class AlbumAdapter extends AbsMediaAdapter<AbsBindAbleHolder, Album> implements FastScrollRecyclerView.SectionedAdapter {
    private static final String TAG = "AlbumAdapter";

    private static final int UN_SET = 0;
    private static final int AVAILABLE = 1;
    private static final int RUNNING = 2;
    private static final String GENRE_UPDATE = "genre_update";
    private final HashMap<Album, GenreArtistTask> mGenreArtistTaskMap = new HashMap<>();
    private ArrayList<Genre>[] mGenres;
    private AlbumClickListener mListener;

    public AlbumAdapter() {
    }

    public static int lighter(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

    public static int lighter(int color, float factor, int alpha) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(alpha, red, green, blue);
    }

    public void setAlbumClickListener(AlbumClickListener listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = null;
    }

    @Override
    protected void onMenuItemClick(int positionInData) {
    }

    @Override
    protected void onDataSet() {
        mGenres = new ArrayList[getData().size()];
        notifyDataSetChanged();
    }

    private void clearAndCancelAllTask() {
        for (Map.Entry<Album, GenreArtistTask> map :
                mGenreArtistTaskMap.entrySet()) {
            GenreArtistTask task = map.getValue();
            if (task != null) task.cancel();
        }

        mGenreArtistTaskMap.clear();
    }

    private void onTaskComplete(Album album, ArrayList<Genre> genres, int positionSaved) {
        mGenreArtistTaskMap.remove(album);
        attachGenreByPosition(genres, album, positionSaved);
    }

    private void attachGenreByPosition(ArrayList<Genre> genres, Album artist, int itemPos) {
        if (itemPos >= 0 && itemPos < getData().size()) {
            if (artist.equals(getData().get(itemPos)) && mGenres[itemPos] == null) {
                mGenres[itemPos] = genres;
                notifyItemChanged(itemPos, GENRE_UPDATE);
            }
        }
    }

    @Override
    public AbsBindAbleHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ItemHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist_child, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AbsBindAbleHolder itemHolder, int i) {
        if (itemHolder instanceof ItemHolder) {
            if (mGenres != null && mGenres.length > i - 1)
                ((ItemHolder) itemHolder).bind(getData().get(i), mGenres[i]);
            else ((ItemHolder) itemHolder).bind(getData().get(i), null);
        }
    }

    @Override
    protected boolean onLongPressedItem(AbsBindAbleHolder holder, int position) {
        super.onLongPressedItem(holder, position);
//        OptionBottomSheet.newInstance(MenuHelper.ARTIST_OPTION, getData().get(position)).show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "artist_option");
        return true;
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        if (getData().get(getDataPosition(i)).getTitle().isEmpty())
            return "";
        return getData().get(getDataPosition(i)).getTitle().substring(0, 1);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsBindAbleHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && holder instanceof ItemHolder) {
            if ((payloads.get(0)).equals(GENRE_UPDATE) && position < mGenres.length)
                ((ItemHolder) holder).bindGenre(mGenres[position]);
        } else
            super.onBindViewHolder(holder, position, payloads);
    }

    public interface AlbumClickListener {
        void onAlbumItemClick(Album artist);
    }

    private static class GenreArtistTask extends AsyncTask<Void, Void, ArrayList<Genre>> {
        private final WeakReference<AlbumAdapter> mAAReference;
        private final int mItemPos;
        private final Album mAlbum;
        private boolean mCancelled = false;

        public GenreArtistTask(AlbumAdapter adapter, Album album, int itemPos) {
            super();
            mAAReference = new WeakReference<>(adapter);
            mAlbum = album;
            mItemPos = itemPos;
        }

        private void cancel() {
            mCancelled = true;
            cancel(true);
            mAAReference.clear();
        }

        @Override
        protected ArrayList<Genre> doInBackground(Void... voids) {

            if (mAAReference.get() != null && mAlbum != null && !mCancelled) {

                return GenreLoader.getGenreForAlbum(mAAReference.get().getContext(), mAlbum.getId());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Genre> genres) {
            if (genres != null && mAAReference.get() != null && !mCancelled) {
                mAAReference.get().onTaskComplete(mAlbum, genres, mItemPos);
            }
        }
    }

    class ItemHolder extends AbsMediaHolder {

        @BindView(R.id.title)
        TextView mArtist;
        @BindView(R.id.image)
        ImageView mImage;
        @BindView(R.id.count)
        TextView mCount;
        @BindView(R.id.llUnivers)
        LinearLayout llUnivers;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnLongClick(R.id.panel)
        boolean onLongClickPanel() {
            return onLongPressedItem(this, getAdapterPosition());
        }

        @OnClick(R.id.panel)
        void goToThisAlbum() {
            if (mListener != null) mListener.onAlbumItemClick(getData().get(getAdapterPosition()));
        }

        public void bind(Album album, ArrayList<Genre> genres) {
            if (album != null) {
                mArtist.setText(album.getTitle());
                Log.e("Album :", album.getArtistName());
                llUnivers.setVisibility(View.GONE);
                mCount.setText(String.format("%d %s", album.getSongCount(), mCount.getContext().getResources().getString(R.string.songs)));

                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri,
                        album.getId());

                Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                        MediaStore.Audio.Albums._ID + "=?",
                        new String[]{String.valueOf(album.getId())},
                        null);

                if (cursor.moveToFirst()) {
                    Glide.with(getContext())
                            .asBitmap()
                            .load(uri)
                            .error(R.drawable.ic_default_album_art)
                            .placeholder(R.drawable.ic_default_album_art)
                            .into(mImage);
                }
            }
        }

        public void bindGenre(ArrayList<Genre> genres) {

        }
    }


}
