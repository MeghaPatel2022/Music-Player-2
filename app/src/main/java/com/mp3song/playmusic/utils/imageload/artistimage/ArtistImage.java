package com.mp3song.playmusic.utils.imageload.artistimage;

public class ArtistImage {
    public static final int FIRST = 0;
    public static final int RANDOM = -1;
    public static final int LOCAL = -2;

    public final String mArtistName;
    public final boolean mSkipOkHttpCache;
    public final boolean mLoadOriginal;
    public final int mImageNumber;

    public ArtistImage(String artistName, boolean skipOkHttpCache, boolean loadOriginal, int imageNumber) {
        this.mArtistName = artistName;
        this.mSkipOkHttpCache = skipOkHttpCache;
        this.mLoadOriginal = loadOriginal;
        this.mImageNumber = imageNumber;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public boolean isSkipOkHttpCache() {
        return mSkipOkHttpCache;
    }
}
