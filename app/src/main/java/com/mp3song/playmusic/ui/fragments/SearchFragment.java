package com.mp3song.playmusic.ui.fragments;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.databinding.FragmentSearchBinding;
import com.mp3song.playmusic.medialoader.SongLoader;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.ui.MusicServiceNavigationFragment;
import com.mp3song.playmusic.ui.adapter.SearchSongChildAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SearchFragment extends MusicServiceNavigationFragment {

    private final static String TAG = SearchFragment.class.getSimpleName();
    private final SearchSongChildAdapter mAdapter = new SearchSongChildAdapter();
    private FragmentSearchBinding searchBinding;

    public SearchFragment() {

    }


    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        // Inflate the layout for this fragment
        searchBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        return searchBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(searchBinding.recyclerView, (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(),
                    0,
                    insets.getSystemWindowInsetRight(),
                    (int) (insets.getSystemWindowInsetBottom() + v.getResources().getDimension(R.dimen.bottom_back_stack_spacing)));
            return ViewCompat.onApplyWindowInsets(v, insets);
        });

        searchBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        searchBinding.recyclerView.setAdapter(mAdapter);

        EditText searchEditText = searchBinding.searchSong.findViewById(androidx.appcompat.R.id.search_src_text);
        Typeface myFont = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myFont = getResources().getFont(R.font.poppins_light);
            searchEditText.setTypeface(myFont);
        }

        int colorInt = getResources().getColor(R.color.text_color_back);
        ColorStateList csl = ColorStateList.valueOf(colorInt);

        ImageView searchIcon = searchBinding.searchSong.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            searchIcon.setImageTintList(csl);
        }

        onBack();
        searchBinding.back.setOnClickListener(v -> {
            if (MainActivity.mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                MainActivity.mainPopUpBehaviour.setHideable(true);
                MainActivity.mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                HomeFragment sf = HomeFragment.newInstance();
                MainActivity activity = (MainActivity) requireContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(null).commit();
            }
        });

        searchBinding.searchSong.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                Log.e("LLL_search: ", query);
                mAdapter.getFilter().filter(query);
                return true;
            }
        });

        refreshData();
    }

    private void onBack() {
        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (MainActivity.mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    MainActivity.mainPopUpBehaviour.setHideable(true);
                    MainActivity.mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else {
                    HomeFragment sf = HomeFragment.newInstance();
                    MainActivity activity = (MainActivity) requireContext();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(null).commit();
                }
                return true;
            }
            return false;
        });
    }

    private void refreshData() {
        ArrayList<Song> songs = SongLoader.getAllSongs(requireActivity());
        mAdapter.setDataAdapter(songs);
    }
}