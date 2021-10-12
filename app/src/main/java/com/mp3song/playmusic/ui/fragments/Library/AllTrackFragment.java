package com.mp3song.playmusic.ui.fragments.Library;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.contract.AbsMediaAdapter;
import com.mp3song.playmusic.databinding.FragmentAllTrackBinding;
import com.mp3song.playmusic.medialoader.SongLoader;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.ui.MusicServiceFragment;
import com.mp3song.playmusic.ui.adapter.PreviewRandomPlayAdapter;
import com.mp3song.playmusic.ui.adapter.SongChildAdapter;
import com.mp3song.playmusic.ui.fragments.HomeFragment;
import com.mp3song.playmusic.utils.Tool;
import com.mp3song.playmusic.utils.Util;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllTrackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllTrackFragment extends MusicServiceFragment implements PreviewRandomPlayAdapter.FirstItemCallBack {

    public static final String TAG = "AllTrackTab";
    private final SongChildAdapter mAdapter = new SongChildAdapter();
    private FragmentAllTrackBinding allTrackBinding;

    public AllTrackFragment() {
        // Required empty public constructor
    }

    public static AllTrackFragment newInstance(String param1, String param2) {
        AllTrackFragment fragment = new AllTrackFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        allTrackBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_track, container, false);
        return allTrackBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        allTrackBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        allTrackBinding.recyclerView.setAdapter(mAdapter);

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

        refreshData();
    }

    @Override
    public void onDestroyView() {
        mAdapter.destroy();
        super.onDestroyView();
    }

    private void refreshData() {
        ArrayList<Song> songs = SongLoader.getAllSongs(requireActivity());
        mAdapter.setData(songs);
    }

    @Override
    public void onFirstItemCreated(Song song) {

    }

    @Override
    public void onPlayingMetaChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        FastScrollRecyclerView recyclerView = allTrackBinding.recyclerView;
        recyclerView.setPopupBgColor(Tool.getHeavyColor());
        recyclerView.setThumbColor(Tool.getHeavyColor());
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onMediaStoreChanged() {
        ArrayList<Song> songs = SongLoader.getAllSongs(requireActivity());
        mAdapter.setData(songs);
    }

}