package com.mp3song.playmusic.ui.adapter.Library;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mp3song.playmusic.R;
import com.mp3song.playmusic.ui.fragments.Library.AlbumFragment;
import com.mp3song.playmusic.ui.fragments.Library.AllTrackFragment;
import com.mp3song.playmusic.ui.fragments.Library.ArtistFragment;

import java.util.ArrayList;

public class LibraryPagerAdapter extends FragmentPagerAdapter {
    private final Context mContext;
    private final ArrayList<Fragment> mData = new ArrayList<>();

    public LibraryPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
        initData();
    }

    public ArrayList<Fragment> getData() {
        return mData;
    }

    private void initData() {
        mData.add(new AllTrackFragment());
        mData.add(new AlbumFragment());
        mData.add(new ArtistFragment());
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return mData.size();
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        return mData.get(position);
    }

    // Returns the page mTitle for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.songs);
            case 1:
                return mContext.getResources().getString(R.string.albums);
            case 2:
                return mContext.getResources().getString(R.string.artists);
            case 3:
                return mContext.getResources().getString(R.string.genres);
            case 4:
                return mContext.getResources().getString(R.string.folders);
            default:
                return null;
        }

    }
}
