package com.mp3song.playmusic.ui.fragments.Library;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mp3song.playmusic.App;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.contract.AbsMediaAdapter;
import com.mp3song.playmusic.medialoader.AlbumLoader;
import com.mp3song.playmusic.models.Album;
import com.mp3song.playmusic.models.Genre;
import com.mp3song.playmusic.ui.MusicServiceFragment;
import com.mp3song.playmusic.ui.NavigationFragment;
import com.mp3song.playmusic.ui.adapter.album.AlbumAdapter;
import com.mp3song.playmusic.ui.fragments.HomeFragment;
import com.mp3song.playmusic.ui.fragments.LibraryTabFragment;
import com.mp3song.playmusic.ui.fragments.album.AlbumPagerFragment;
import com.mp3song.playmusic.utils.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AlbumFragment extends MusicServiceFragment implements AlbumAdapter.AlbumClickListener {

    public static final String TAG = "AlbumTab";

    private final AlbumAdapter mAdapter = new AlbumAdapter();
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @Nullable
    @BindView(R.id.swipe_refresh)
    Unbinder mUnBinder;
    private LoadArtistAsyncTask mLoadArtist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
        mAdapter.setAlbumClickListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnBinder = ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((v, keyCode, event) -> {
            Util.lastPos = 0;
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
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

        refresh();

    }

    @Override
    public void onDestroyView() {

        if (mLoadArtist != null) mLoadArtist.cancel(true);
        mAdapter.destroy();
        if (mUnBinder != null)
            mUnBinder.unbind();

        super.onDestroyView();
    }

    private void refresh() {

        if (mLoadArtist != null) mLoadArtist.cancel(true);
        mLoadArtist = new LoadArtistAsyncTask(this);
        mLoadArtist.execute();

    }

    @Override
    public void onAlbumItemClick(Album artist) {
        NavigationFragment sf = AlbumPagerFragment.newInstance(artist);
        String backStateName = LibraryTabFragment.class.getName();
        MainActivity activity = (MainActivity) requireContext();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(null).commit();
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayingMetaChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
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
        refresh();
    }

    private static class AsyncResult {
        private ArrayList<Genre>[] mGenres;
        private List<Album> mArtist;
    }

    private static class LoadArtistAsyncTask extends AsyncTask<Void, Void, AsyncResult> {
        private final WeakReference<AlbumFragment> mFragment;
        private boolean mCancelled = false;

        public LoadArtistAsyncTask(AlbumFragment fragment) {
            super();
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected AsyncResult doInBackground(Void... voids) {
            AsyncResult result = new AsyncResult();
            Context context = null;

            if (App.getInstance() != null)
                context = App.getInstance().getApplicationContext();

            if (context != null)
                result.mArtist = AlbumLoader.getAllAlbums(App.getInstance());
            else return null;


            return result;
        }

        public void cancel() {
            mCancelled = true;
            cancel(true);
            mFragment.clear();
        }

        @Override
        protected void onPostExecute(AsyncResult asyncResult) {
            if (mCancelled) return;
            AlbumFragment fragment = mFragment.get();
            if (fragment != null && !fragment.isDetached()) {
                if (!asyncResult.mArtist.isEmpty()) {
                    fragment.mAdapter.setData(asyncResult.mArtist);
                }
                fragment.mLoadArtist = null;
            }
        }
    }

}