package com.mp3song.playmusic.helpers.songpreview;

public interface SongPreviewListener {
    void onSongPreviewStart(PreviewSong song);

    void onSongPreviewFinish(PreviewSong song);
}