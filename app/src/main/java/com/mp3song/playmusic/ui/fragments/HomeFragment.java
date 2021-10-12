package com.mp3song.playmusic.ui.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.mp3song.playmusic.App;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.databinding.FragmentHomeBinding;
import com.mp3song.playmusic.medialoader.ArtistLoader;
import com.mp3song.playmusic.medialoader.PlaylistLoader;
import com.mp3song.playmusic.models.Artist;
import com.mp3song.playmusic.models.Playlist;
import com.mp3song.playmusic.service.MusicPlayerRemote;
import com.mp3song.playmusic.service.MusicServiceEventListener;
import com.mp3song.playmusic.ui.AddMultipleSong;
import com.mp3song.playmusic.ui.MusicServiceActivity;
import com.mp3song.playmusic.ui.NavigationFragment;
import com.mp3song.playmusic.ui.adapter.PlaylistChildAdapter;
import com.mp3song.playmusic.ui.pages.CardLayerFragment;
import com.mp3song.playmusic.utils.PlaylistsUtil;
import com.mp3song.playmusic.utils.imageload.ArtistGlideRequest;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends CardLayerFragment implements MusicServiceEventListener, PlaylistChildAdapter.PlaylistClickListener {

    private static final String TAG = "BackStackController";
    private final PlaylistChildAdapter mAdapter = new PlaylistChildAdapter();
    public FragmentHomeBinding homeBinding;
    private boolean mIsUsingAIAsBg = true;
    private Dialog dialog;

    private RefreshPlayListReceiver refreshPlayListReceiver;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeBinding.rvPlayList.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        homeBinding.rvPlayList.setAdapter(mAdapter);
        homeBinding.scrollable.smoothScrollTo(0, 0);
        refreshPlayListReceiver = new RefreshPlayListReceiver();

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshPlayListReceiver,
                new IntentFilter("REFRESH_PLAYLIST"));

        refreshData();
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setShowAuto(true);
        mAdapter.setOnItemClickListener(this);
        if (getActivity() instanceof MusicServiceActivity) {
            ((MusicServiceActivity) getActivity()).addMusicServiceEventListener(this);
        }
    }

    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container) {
        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        homeBinding.imgSearch.setOnClickListener(v -> {
            NavigationFragment sf = SearchFragment.newInstance();
            String backStateName = HomeFragment.class.getName();
            MainActivity activity = (MainActivity) requireContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
        });

        homeBinding.llLibrary.setOnClickListener(v -> {
            NavigationFragment sf = LibraryTabFragment.newInstance();
            String backStateName = HomeFragment.class.getName();
            MainActivity activity = (MainActivity) requireContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
        });

        homeBinding.llRecentlyPlay.setOnClickListener(v -> {
            Playlist playlist = PlaylistLoader.getAllPlaylistsWithAuto(requireActivity()).get(1);
            String backStateName = HomeFragment.class.getName();
            Bitmap bitmap = BitmapFactory.decodeResource(requireActivity().getResources(), R.drawable.ic_default_album_art);
            NavigationFragment sf = SinglePlaylistFragment.newInstance(false, playlist, bitmap);
            MainActivity activity = (MainActivity) requireContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
        });

        homeBinding.llLastAdded.setOnClickListener(v -> {
            Playlist playlist = PlaylistLoader.getAllPlaylistsWithAuto(requireActivity()).get(0);
            String backStateName = HomeFragment.class.getName();
            Bitmap bitmap = BitmapFactory.decodeResource(requireActivity().getResources(), R.drawable.ic_default_album_art);
            NavigationFragment sf = SinglePlaylistFragment.newInstance(false, playlist, bitmap);
            MainActivity activity = (MainActivity) requireContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
        });

        homeBinding.llMostPlay.setOnClickListener(v -> {
            Playlist playlist = PlaylistLoader.getAllPlaylistsWithAuto(requireActivity()).get(2);
            String backStateName = HomeFragment.class.getName();
            Bitmap bitmap = BitmapFactory.decodeResource(requireActivity().getResources(), R.drawable.ic_default_album_art);
            NavigationFragment sf = SinglePlaylistFragment.newInstance(false, playlist, bitmap);
            MainActivity activity = (MainActivity) requireContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
        });

        homeBinding.llFavourite.setOnClickListener(v -> {
            String backStateName = HomeFragment.class.getName();
            NavigationFragment sf = FavouriteFragment.newInstance();
            MainActivity activity = (MainActivity) requireContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
        });

        return homeBinding.getRoot();
    }

    private class RefreshPlayListReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshData();
        }
    }

    private void refreshData() {
        Playlist playlist = new Playlist(0, requireActivity().getResources().getString(R.string.action_new_playlist));
        final List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);
        playlists.addAll(PlaylistLoader.getAllPlaylists(requireActivity()));
        homeBinding.tvPlayListCount.setText(requireActivity().getString(R.string.playlist_count, PlaylistLoader.getAllPlaylists(requireActivity()).size()));
        mAdapter.setData(playlists);
    }

    @Override
    public int getLayerMinHeight(Context context, int maxHeight) {
        return maxHeight;
    }

    @Override
    public String getCardLayerTag() {
        return TAG;
    }

    @Override
    public boolean isFullscreenLayer() {
        return true;
    }

    @Override
    public void onPlayingMetaChanged() {
        requireContext();
        Artist artist = ArtistLoader.getArtist(requireContext(), MusicPlayerRemote.getCurrentSong().artistId);
        if (homeBinding.backImage.getVisibility() == View.VISIBLE)
            ArtistGlideRequest.Builder.from(Glide.with(requireContext()), artist)
                    .tryToLoadOriginal(true)
                    .generateBuilder(getContext())
                    .build()
                    .thumbnail(
                            ArtistGlideRequest
                                    .Builder
                                    .from(Glide.with(requireContext()), artist)
                                    .tryToLoadOriginal(false)
                                    .generateBuilder(getContext())
                                    .build())
                    .into(homeBinding.backImage);
    }

    public void onUsingArtistImagePreferenceChanged() {
        mIsUsingAIAsBg = App.getInstance().getPreferencesUtility().isUsingArtistImageAsBackground();
        if (mIsUsingAIAsBg) {
            homeBinding.backImage.setVisibility(View.VISIBLE);
            onPlayingMetaChanged();
        } else {
            homeBinding.backImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickPlaylist(Playlist playlist, @org.jetbrains.annotations.Nullable Bitmap bitmap) {
        if (playlist.name.equals(requireActivity().getResources().getString(R.string.action_new_playlist))) {
            addToPlayList();
        } else {
            String backStateName = HomeFragment.class.getName();
            NavigationFragment sf = SinglePlaylistFragment.newInstance(true, playlist, bitmap);
            MainActivity activity = (MainActivity) requireContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
        }
    }

    private void addToPlayList() {
        dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_create_playlist);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        EditText etPlayListName = dialog.findViewById(R.id.etPlayListName);
        dialog.findViewById(R.id.tvCreate).setOnClickListener(v -> {
            int playListID = PlaylistsUtil.createPlaylist(requireActivity(), etPlayListName.getText().toString());
            Log.e("LLL_PlayListID: ", String.valueOf(playListID));
            requireActivity().runOnUiThread(this::refreshData);
            if (dialog.isShowing())
                dialog.dismiss();
            Intent intent = new Intent(requireActivity(), AddMultipleSong.class);
            intent.putExtra("playlist", playListID);
            startActivity(intent);
        });

        dialog.findViewById(R.id.tvCancel).setOnClickListener(v -> {
            if (dialog.isShowing())
                dialog.dismiss();
        });

        dialog.show();
    }

    public boolean streamOnTouchEvent(MotionEvent event) {
        return mCardLayerController.dispatchOnTouchEvent(homeBinding.root, event);
    }
}