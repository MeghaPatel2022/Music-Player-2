package com.mp3song.playmusic.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.mp3song.playmusic.MainActivity;
import com.mp3song.playmusic.R;
import com.mp3song.playmusic.databinding.ScreenTabLibraryBinding;
import com.mp3song.playmusic.ui.NavigationFragment;
import com.mp3song.playmusic.ui.adapter.Library.LibraryPagerAdapter;
import com.mp3song.playmusic.ui.fragments.Library.AlbumFragment;
import com.mp3song.playmusic.ui.fragments.Library.AllTrackFragment;
import com.mp3song.playmusic.ui.fragments.Library.ArtistFragment;
import com.mp3song.playmusic.utils.Util;

public class LibraryTabFragment extends NavigationFragment {

    private static final String TAG = "LibraryTabFragment";
    private ScreenTabLibraryBinding tabLibraryBinding;
    private LibraryPagerAdapter mPagerAdapter;

    public static LibraryTabFragment newInstance() {
        LibraryTabFragment fragment = new LibraryTabFragment();
        return fragment;
    }

    public ViewPager getViewPager() {
        return tabLibraryBinding.viewPager;
    }

    public LibraryPagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        tabLibraryBinding = DataBindingUtil.inflate(inflater, R.layout.screen_tab_library, container, false);

        return tabLibraryBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        tabLibraryBinding.getRoot().setFocusableInTouchMode(true);
        tabLibraryBinding.getRoot().requestFocus();
        tabLibraryBinding.getRoot().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                Log.e("LLL_Back: ", String.valueOf(Util.lastPos));
                if (Util.lastPos == 0) {
                    String backStateName = HomeFragment.class.getName();
                    HomeFragment sf = HomeFragment.newInstance();
                    MainActivity activity = (MainActivity) requireContext();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
                } else {
                    Util.lastPos = 0;
                    tabLibraryBinding.viewPager.setCurrentItem(Util.lastPos);
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLibraryBinding.viewPager.setOnTouchListener((v, event) -> getMainActivity().backStackStreamOnTouchEvent(event));
        mPagerAdapter = new LibraryPagerAdapter(getActivity(), getChildFragmentManager());
        tabLibraryBinding.viewPager.setAdapter(mPagerAdapter);
        tabLibraryBinding.viewPager.setOffscreenPageLimit(3);
        tabLibraryBinding.tabLayout.setupWithViewPager(tabLibraryBinding.viewPager);
        tabLibraryBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLibraryBinding.tabLayout));
        tabLibraryBinding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(tabLibraryBinding.viewPager));

        tabLibraryBinding.getRoot().setFocusableInTouchMode(true);
        tabLibraryBinding.getRoot().requestFocus();
        tabLibraryBinding.getRoot().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                Log.e("LLL_Back: ", String.valueOf(Util.lastPos));
                if (Util.lastPos == 0) {
                    String backStateName = HomeFragment.class.getName();
                    HomeFragment sf = HomeFragment.newInstance();
                    MainActivity activity = (MainActivity) requireContext();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.layer_container, sf).addToBackStack(backStateName).commit();
                } else {
                    Util.lastPos = 0;
                    tabLibraryBinding.viewPager.setCurrentItem(Util.lastPos);
                }
                return true;
            }
            return false;
        });

        tabLibraryBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (MainActivity.mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    MainActivity.mainPopUpBehaviour.setHideable(true);
                    MainActivity.mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else if (MainActivity.mainPopUpBehaviour1.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    MainActivity.mainPopUpBehaviour1.setHideable(true);
                    MainActivity.mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLibraryBinding.viewPager.setCurrentItem(Util.lastPos);
    }

    public Fragment navigateToTab(int item) {
        if (item < mPagerAdapter.getCount()) {
            tabLibraryBinding.viewPager.setCurrentItem(item, false);
            return mPagerAdapter.getItem(item);
        }
        return null;
    }

    public Fragment navigateToTab(final String tag) {
        switch (tag) {
            case AllTrackFragment.TAG:
                return navigateToTab(0);
            case AlbumFragment.TAG:
                return navigateToTab(1);
            case ArtistFragment.TAG:
                return navigateToTab(2);
            default:
                return null;
        }
    }
}
