package com.mp3song.playmusic;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mp3song.playmusic.databinding.ActivityMainBinding;
import com.mp3song.playmusic.helpers.songpreview.SongPreviewController;
import com.mp3song.playmusic.medialoader.PlaylistLoader;
import com.mp3song.playmusic.medialoader.SongLoader;
import com.mp3song.playmusic.models.Artist;
import com.mp3song.playmusic.models.Playlist;
import com.mp3song.playmusic.models.Song;
import com.mp3song.playmusic.service.MusicPlayerRemote;
import com.mp3song.playmusic.service.MusicService;
import com.mp3song.playmusic.ui.CardLayerController;
import com.mp3song.playmusic.ui.MusicServiceActivity;
import com.mp3song.playmusic.ui.adapter.PlaylistChildListAdapter;
import com.mp3song.playmusic.ui.fragments.HomeFragment;
import com.mp3song.playmusic.ui.fragments.NowPlayingFragment;
import com.mp3song.playmusic.ui.fragments.NowPlayingLayerFragment;
import com.mp3song.playmusic.utils.MusicUtil;
import com.mp3song.playmusic.utils.PlaylistsUtil;
import com.mp3song.playmusic.utils.RingtoneManager;
import com.mp3song.playmusic.utils.SharedPrefrences;
import com.mp3song.playmusic.utils.Util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class MainActivity extends MusicServiceActivity {

    private static final String TAG = "AppActivity";
    private static final int CODE_PERMISSIONS_WRITE_STORAGE = 1;
    public static BottomSheetBehavior mainPopUpBehaviour;
    public static BottomSheetBehavior mainPopUpBehaviour1;
    public static BottomSheetBehavior mainPopUpBehaviour2;
    private final int[] mCurrentSystemInsets = new int[]{0, 0, 0, 0};
    private final boolean mUseDynamicTheme = true;
    private final PlaylistChildListAdapter mAdapter = new PlaylistChildListAdapter();
    public ActivityMainBinding mainBinding;
    public HomeFragment mBackStackController;
    public NowPlayingLayerFragment mNowPlayingController;
    public NowPlayingFragment mNowPlayingFragment;
    public CardLayerController mCardLayerController;
    MyClickHandlers myClickHandlers;
    private PermissionListener mPermissionListener;
    private MenuOpenReceiver menuOpenReceiver;
    private Dialog dialog;
    private Dialog dialog1;
    private Dialog deleteDialog;
    private Dialog infoDialog;
    private Object songMenu;
    private String receive;

    public CardLayerController getCardLayerController() {
        return mCardLayerController;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        switch (requestCode) {
            case CODE_PERMISSIONS_WRITE_STORAGE: {
                if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // Granted
                        onPermissionGranted();
                    } else onPermissionDenied();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @NonNull
    public final int[] getCurrentSystemInsets() {
        return mCurrentSystemInsets;
    }

    public void setPermissionListener(PermissionListener listener) {
        mPermissionListener = listener;
    }

    public void removePermissionListener() {
        mPermissionListener = null;
    }

    private void onPermissionGranted() {
        if (mPermissionListener != null) mPermissionListener.onPermissionGranted();
    }

    private void onPermissionDenied() {
        if (mPermissionListener != null) mPermissionListener.onPermissionDenied();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);

        mAdapter.init(MainActivity.this);
        mAdapter.setShowAuto(true);

        myClickHandlers = new MyClickHandlers(MainActivity.this);
        mainBinding.setOnClick(myClickHandlers);
        mainBinding.llMain.setOnClick(myClickHandlers);
        mainBinding.llMainAlbum.setOnClick(myClickHandlers);
        mainBinding.llMainPlayList.setOnClick(myClickHandlers);

        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            mCurrentSystemInsets[0] = insets.getSystemWindowInsetLeft();
            mCurrentSystemInsets[1] = insets.getSystemWindowInsetTop();
            mCurrentSystemInsets[2] = insets.getSystemWindowInsetRight();
            mCurrentSystemInsets[3] = insets.getSystemWindowInsetBottom();
            return ViewCompat.onApplyWindowInsets(v, insets);
        });
        mainBinding.appRoot.post(() -> {
            boolean isPermissionGranted = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                isPermissionGranted = checkSelfPermission();
            }
            if (!isPermissionGranted) {

            } else {
                showMainUI();
            }
            if (mUseDynamicTheme) {
                mainBinding.appRoot.postDelayed(() ->
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                        , 2500);
            }
        });

        mainPopUpBehaviour = BottomSheetBehavior.from(mainBinding.llMain.llMainPopup);
        mainPopUpBehaviour1 = BottomSheetBehavior.from(mainBinding.llMainAlbum.llMainPopup);
        mainPopUpBehaviour2 = BottomSheetBehavior.from(mainBinding.llMainPlayList.llMainPopup);

        mainPopUpBehaviour.setHideable(true);
        mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);

        mainPopUpBehaviour1.setHideable(true);
        mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);

        mainPopUpBehaviour2.setHideable(true);
        mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_HIDDEN);

        mainPopUpBehaviour2.setDraggable(false);
        mainPopUpBehaviour.setDraggable(false);
        mainPopUpBehaviour1.setDraggable(false);

        menuOpenReceiver = new MenuOpenReceiver();

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(menuOpenReceiver,
                new IntentFilter("REFRESH"));
    }

    public boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void showMainUI() {
        /* remove the intro navigation */
        //runLoading();
        mCardLayerController = new CardLayerController(this);
        mBackStackController = new HomeFragment();
        mNowPlayingController = new NowPlayingLayerFragment();
        mNowPlayingFragment = new NowPlayingFragment();

        mCardLayerController.init(mainBinding.layerContainer, mBackStackController, mNowPlayingController);
    }

    @Override
    public void setTheme(@StyleRes final int resId) {
        super.setTheme(resId);
        getDelegate().setTheme(resId);
    }

    public HomeFragment getBackStackController() {
        return mBackStackController;
    }

    public NowPlayingLayerFragment getNowPlayingController() {
        return mNowPlayingController;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: ");
        mainBinding.appRoot.post(() -> handlePlaybackIntent(intent));
    }

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, CODE_PERMISSIONS_WRITE_STORAGE);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        CODE_PERMISSIONS_WRITE_STORAGE);

            }
        } else onPermissionGranted();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        mainBinding.appRoot.post(() -> handlePlaybackIntent(getIntent()));
    }

    @Override
    public void onBackPressed() {
        if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mainPopUpBehaviour.setHideable(true);
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (mainPopUpBehaviour1.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mainPopUpBehaviour1.setHideable(true);
            mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (mainPopUpBehaviour2.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mainPopUpBehaviour2.setHideable(true);
            mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (dialog != null) {
            if (dialog.isShowing())
                dialog.dismiss();
        } else if (dialog1 != null) {
            if (dialog1.isShowing())
                dialog1.dismiss();
        } else if (deleteDialog != null) {
            if (deleteDialog.isShowing())
                deleteDialog.dismiss();
        } else if (infoDialog != null) {
            if (infoDialog.isShowing())
                infoDialog.dismiss();
        } else
            finishAffinity();
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        if (mimeType == null) {
            mimeType = "";
        }
        boolean handled = false;

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            Log.d(TAG, "handlePlaybackIntent: type media play from search");
            handled = true;
        }

        if (uri != null && uri.toString().length() > 0) {
            Log.d(TAG, "handlePlaybackIntent: type play file");
            MusicPlayerRemote.playFromUri(uri);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            Log.d(TAG, "handlePlaybackIntent: type playlist");
            handled = true;
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            Log.d(TAG, "handlePlaybackIntent: type album");
            handled = true;
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            Log.d(TAG, "handlePlaybackIntent: type artist");
            handled = true;
        } else if (!handled && MusicService.ACTION_ON_CLICK_NOTIFICATION.equals(intent.getAction())) {
            handled = true;
        } else if (!handled) {
            Log.d(TAG, "handlePlaybackIntent: unhandled: " + intent.getAction());
        }

        if (handled) {
            setIntent(new Intent());
        }
    }

    public boolean backStackStreamOnTouchEvent(MotionEvent event) {
        if (mBackStackController != null) return mBackStackController.streamOnTouchEvent(event);
        return false;
    }


    private void addToPlayListDialog(String from) {
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_add_to_playlist);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        refreshData();

        RecyclerView rvPlayList = dialog.findViewById(R.id.rvPlayList);
        rvPlayList.setLayoutManager(new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false));
        rvPlayList.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((playlist, bitmap) -> {
            Log.e("LLL_PlayList_: ", playlist.id + " Name: " + playlist.name);
            if (from.equals("song")) {
                if (playlist.name.equals(getResources().getString(R.string.action_new_playlist))) {
                    if (dialog != null) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                    addToPlayList("song");
                } else {
                    PlaylistsUtil.addToPlaylist(MainActivity.this, (Song) songMenu, playlist.id, true);
                }
                if (dialog != null) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                }
            } else if (from.equals("album")) {
                Artist artist = (Artist) songMenu;
                if (playlist.name.equals(getResources().getString(R.string.action_new_playlist))) {
                    if (dialog != null) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                    addToPlayList("album");
                } else {
                    PlaylistsUtil.addToPlaylist(MainActivity.this, new ArrayList<>(artist.getSongs()), playlist.id, true);
                }
                if (dialog != null) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                }
            } else if (from.equals("PlayList")) {
                if (playlist.name.equals(getResources().getString(R.string.action_new_playlist))) {
                    if (dialog != null) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                    addToPlayList("PlayList");
                } else {
                    PlaylistsUtil.addToPlaylist(MainActivity.this, new ArrayList<>(MusicUtil.getPlaylistSongList(MainActivity.this, (Playlist) songMenu)), playlist.id, true);
                }
                if (dialog != null) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                }
            }
        });

        TextView tvCancel = dialog.findViewById(R.id.tvCancel);
        tvCancel.setOnClickListener(v -> {
            if (dialog != null) {
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addToPlayList(String from) {
        dialog1 = new Dialog(MainActivity.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.setContentView(R.layout.dialog_create_playlist);
        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.setCancelable(true);
        dialog1.setCanceledOnTouchOutside(true);

        EditText etPlayListName = dialog1.findViewById(R.id.etPlayListName);
        TextView tvCreate = dialog1.findViewById(R.id.tvCreate);

        if (from.equals("Rename")) {
            Playlist playlist = (Playlist) songMenu;
            etPlayListName.setText(playlist.name);
            tvCreate.setText("Rename");
        }

        tvCreate.setOnClickListener(v -> {
            if (from.equals("song")) {
                int playListID = PlaylistsUtil.createPlaylist(MainActivity.this, etPlayListName.getText().toString());
                Log.e("LLL_PlayListID: ", String.valueOf(playListID));
                runOnUiThread(this::refreshData);
                PlaylistsUtil.addToPlaylist(MainActivity.this, (Song) songMenu, playListID, true);
            } else if (from.equals("album")) {
                int playListID = PlaylistsUtil.createPlaylist(MainActivity.this, etPlayListName.getText().toString());
                Log.e("LLL_PlayListID: ", String.valueOf(playListID));
                runOnUiThread(this::refreshData);
                Artist artist = (Artist) songMenu;
                PlaylistsUtil.addToPlaylist(MainActivity.this, new ArrayList<>(artist.getSongs()), playListID, true);
            } else if ((from.equals("PlayList"))) {
                int playListID = PlaylistsUtil.createPlaylist(MainActivity.this, etPlayListName.getText().toString());
                Log.e("LLL_PlayListID: ", String.valueOf(playListID));
                runOnUiThread(this::refreshData);
                Playlist playlist = (Playlist) songMenu;
                PlaylistsUtil.addToPlaylist(MainActivity.this, new ArrayList<>(MusicUtil.getPlaylistSongList(MainActivity.this, (Playlist) songMenu)), playListID, true);
            } else if (from.equals("Rename")) {
                tvCreate.setText("Rename");
                final String name = etPlayListName.getText().toString().trim();
                if (!name.isEmpty()) {
                    Playlist playlist = (Playlist) songMenu;
                    long playlistId1 = playlist.id;
                    PlaylistsUtil.renamePlaylist(MainActivity.this, playlistId1, name);
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
                    Intent localIn;
                    localIn = new Intent("REFRESH_PLAYLIST");
                    lbm.sendBroadcast(localIn);
                }
            }

            if (dialog1.isShowing())
                dialog1.dismiss();
        });

        dialog1.findViewById(R.id.tvCancel).setOnClickListener(v -> {
            if (dialog1.isShowing())
                dialog1.dismiss();
        });

        dialog1.show();
    }

    private void deleteSong(ArrayList<Song> songs) {
        deleteDialog = new Dialog(MainActivity.this);
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteDialog.setContentView(R.layout.dialog_delete);
        deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteDialog.setCancelable(true);
        deleteDialog.setCanceledOnTouchOutside(true);

        TextView tvCreate = deleteDialog.findViewById(R.id.tvCreate);
        TextView tvRingtoneTitle = deleteDialog.findViewById(R.id.tvRingtoneTitle);
        TextView tvRingtoneDesc = deleteDialog.findViewById(R.id.tvRingtoneDesc);
        if (receive.equals("playListItem")) {
            tvCreate.setText("Remove");
            if (songs.size() > 1) {
                tvRingtoneTitle.setText("Remove Songs?");
                tvRingtoneDesc.setText(Html.fromHtml(getString(R.string.remove_x_songs_from_playlist, songs.size())));
            } else {
                tvRingtoneTitle.setText("Remove Song?");
                tvRingtoneDesc.setText(Html.fromHtml(getString(R.string.remove_song_x_from_playlist, songs.get(0).title)));
            }
        } else if (receive.equals("PlayList")) {
            tvCreate.setText("Delete");
            tvRingtoneTitle.setText("Delete Playlist?");
            tvRingtoneDesc.setText(Html.fromHtml(getString(R.string.delete_playlist_x, ((Playlist) songMenu).name)));
        } else {
            tvCreate.setText("Delete");
            if (songs.size() > 1) {
                tvRingtoneTitle.setText("Delete Songs?");
                tvRingtoneDesc.setText(Html.fromHtml(getString(R.string.delete_x_songs, songs.size())));
            } else {
                tvRingtoneTitle.setText("Delete Song?");
                tvRingtoneDesc.setText(Html.fromHtml(getString(R.string.delete_song_x, songs.get(0).title)));
            }
        }

        tvCreate.setOnClickListener(v -> {
            if (receive.equals("playListItem")) {
                PlaylistsUtil.removeFromPlaylist(MainActivity.this, songs.get(0), Util.playListId);
            } else if (receive.equals("PlayList")) {
                ArrayList<Playlist> list = new ArrayList<>();
                list.add((Playlist) songMenu);
                PlaylistsUtil.deletePlaylists(MainActivity.this, list);
            } else {
                MusicUtil.deleteTracks(MainActivity.this, songs);
            }
            if (deleteDialog.isShowing())
                deleteDialog.dismiss();
        });

        deleteDialog.findViewById(R.id.tvCancel).setOnClickListener(v -> {
            if (deleteDialog.isShowing())
                deleteDialog.dismiss();
        });

        deleteDialog.show();
    }

    private void infoSong(Song songs) {
        infoDialog = new Dialog(MainActivity.this);
        infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        infoDialog.setContentView(R.layout.dialog_information);
        infoDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoDialog.setCancelable(true);
        infoDialog.setCanceledOnTouchOutside(true);

        TextView tvTrack = infoDialog.findViewById(R.id.tvTrack);
        TextView tvAlbumName = infoDialog.findViewById(R.id.tvAlbumName);
        TextView tvArtistName = infoDialog.findViewById(R.id.tvArtistName);
        TextView tvTime = infoDialog.findViewById(R.id.tvTime);

        tvTrack.setText(songs.title);
        tvAlbumName.setText(songs.albumName);
        tvArtistName.setText(songs.artistName);

        int dur_minute = (int) (songs.duration / 1000 / 60);
        int dur_second = (int) (songs.duration / 1000 - dur_minute * 60);

        String text1 = "";
        if (dur_minute < 10) text1 += "0";
        text1 += dur_minute + ":";
        if (dur_second < 10) text1 += "0";
        text1 += dur_second;

        tvTime.setText(text1);

        infoDialog.findViewById(R.id.tvClose).setOnClickListener(v -> {
            if (infoDialog.isShowing())
                infoDialog.dismiss();
        });

        infoDialog.show();
    }

    private void refreshData() {
        Playlist playlist = new Playlist(0, getResources().getString(R.string.action_new_playlist));
        final List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);
        playlists.addAll(PlaylistLoader.getAllPlaylists(MainActivity.this));

        mAdapter.setData(playlists);
    }

    public interface PermissionListener {
        void onPermissionGranted();

        void onPermissionDenied();
    }

    private class MenuOpenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            receive = intent.getStringExtra("receive");
            if (receive != null) {
                switch (receive) {
                    case "songsItem":
                    case "playListItem":
                        Log.e("LLL_PlayList: ", "Done");
                        if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                        if (mainPopUpBehaviour1.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }

                        if (intent.getSerializableExtra("object") != null) {
                            songMenu = intent.getSerializableExtra("object");
                            Song song = (Song) songMenu;
                            mainBinding.llMain.tvSongName.setText(song.title);
                        }

                        Song song = (Song) songMenu;

                        boolean exist = false;
                        int pos = 0;
                        List<Song> helperList;
                        helperList = SharedPrefrences.getFavouriteList(MainActivity.this);
                        if (helperList != null && helperList.size() > 0) {
                            for (int i = 0; i < helperList.size(); i++) {
                                Song song1 = helperList.get(i);
                                if (song1.id == song.id) {
                                    exist = true;
                                    pos = i;
                                    break;
                                } else {
                                    exist = false;
                                }
                            }
                        }
                        if (!exist) {
                            helperList.add(song);
                            mainBinding.llMain.imgFav.setSelected(true);
                        } else {
                            helperList.remove(pos);
                            mainBinding.llMain.imgFav.setSelected(false);
                        }

                        break;
                    case "artistItem":
                        if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
                        if (mainPopUpBehaviour1.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                            mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }

                        if (intent.getSerializableExtra("object") != null) {
                            songMenu = intent.getSerializableExtra("object");
                            Artist artist = (Artist) songMenu;
                            mainBinding.llMainAlbum.tvAlbumName.setText(artist.getName());
                        }
                        break;
                    case "PlayList":
                        if (mainPopUpBehaviour2.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                            mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }

                        if (intent.getSerializableExtra("object") != null) {
                            songMenu = intent.getSerializableExtra("object");
                            Playlist playlist = (Playlist) songMenu;
                            mainBinding.llMainPlayList.tvAlbumName.setText(playlist.name);
                        }
                        break;
                }
            }
        }
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void addToFavourite(View view) {
            if (receive != null) {
                mainPopUpBehaviour.setHideable(true);
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                Song song = (Song) songMenu;

                boolean exist = false;
                int pos = 0;
                List<Song> helperList;
                helperList = SharedPrefrences.getFavouriteList(MainActivity.this);
                if (helperList != null && helperList.size() > 0) {
                    for (int i = 0; i < helperList.size(); i++) {
                        Song song1 = helperList.get(i);
                        if (song1.id == song.id) {
                            exist = true;
                            pos = i;
                            break;
                        } else {
                            exist = false;
                        }
                    }
                }
                if (!exist) {
                    helperList.add(song);
                    mainBinding.llMain.imgFav.setSelected(true);
                    Toasty.success(MainActivity.this, "Song added into Favourite", Toasty.LENGTH_SHORT).show();
                } else {
                    helperList.remove(pos);
                    mainBinding.llMain.imgFav.setSelected(false);
                    Toasty.info(MainActivity.this, "Song remove from Favourite", Toasty.LENGTH_SHORT).show();
                }

                SharedPrefrences.setFavouriteList(MainActivity.this, helperList);

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
                Intent localIn;
                localIn = new Intent("REFRESH_FAV");
                lbm.sendBroadcast(localIn);
            }
        }

        public void onPlayNext(View view) {
            if (receive != null) {
                mainPopUpBehaviour.setHideable(true);
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                MusicPlayerRemote.playNext((Song) songMenu);
            }
        }

        public void onEnqueue(View view) {
            if (receive != null) {
                mainPopUpBehaviour.setHideable(true);
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                MusicPlayerRemote.enqueue((Song) songMenu);
            }
        }

        public void onAddTo(View view) {
            if (receive != null) {
                mainPopUpBehaviour.setHideable(true);
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                addToPlayListDialog("song");
            }
        }

        public void onSetAsRingtone(View view) {
            mainPopUpBehaviour.setHideable(true);
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            if (RingtoneManager.requiresDialog(MainActivity.this)) {
                RingtoneManager.showDialog(MainActivity.this);
            } else {
                Song song = (Song) songMenu;
                RingtoneManager ringtoneManager = new RingtoneManager();
                Uri uri = ringtoneManager.setRingtone(MainActivity.this, song.id);
                android.media.RingtoneManager.setActualDefaultRingtoneUri(
                        MainActivity.this, android.media.RingtoneManager.TYPE_RINGTONE,
                        uri
                );
            }
        }

        public void onShare(View view) {
            mainPopUpBehaviour.setHideable(true);
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            Song song = (Song) songMenu;
            startActivity(Intent.createChooser(MusicUtil.createShareSongFileIntent(song, MainActivity.this), null));
        }

        public void onDelete(View view) {
            mainPopUpBehaviour.setHideable(true);
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            Song song = (Song) songMenu;
            ArrayList<Song> songArrayList;
            songArrayList = new ArrayList<>();
            songArrayList.add(song);
            deleteSong(songArrayList);
        }

        public void onPlayPreviewAll(View view) {
            mainPopUpBehaviour.setHideable(true);
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            SongPreviewController preview = getSongPreviewController();
            Song song = (Song) songMenu;
            Log.e("LLLL_Prev: ", String.valueOf(preview));
            if (preview != null) {
                if (preview.isPlayingPreview()) {
                    preview.cancelPreview();
                }

                ArrayList<Song> list = SongLoader.getAllSongs(MainActivity.this);
                Collections.shuffle(list);
                int index = 0;
                for (int i = 0; i < list.size(); i++) {
                    if (song.id == list.get(i).id) index = i;
                }

                if (index != 0)
                    list.add(0, list.remove(index));
                preview.previewSongs(list);
                Toasty.success(context, "Play Preview On...", Toasty.LENGTH_SHORT).show();

            } else {
                Toasty.info(context, "Preview is not available...", Toasty.LENGTH_SHORT).show();
            }
        }

        public void onInfo(View view) {
            mainPopUpBehaviour.setHideable(true);
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            SongPreviewController preview = getSongPreviewController();
            Song song = (Song) songMenu;
            infoSong(song);
        }

        public void onPlayNextAl(View view) {
            mainPopUpBehaviour1.setHideable(true);
            mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);
            Artist artist = (Artist) songMenu;
            MusicPlayerRemote.openAndShuffleQueue(artist.getSongs(), true);
        }

        public void onEnqueueAl(View view) {
            mainPopUpBehaviour1.setHideable(true);
            mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);
            Artist artist = (Artist) songMenu;
            MusicPlayerRemote.enqueue(artist.getSongs());
        }

        public void onAddToPlaylistAl(View view) {
            mainPopUpBehaviour1.setHideable(true);
            mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);
            addToPlayListDialog("album");
        }

        public void onPlayPreview(View view) {
            mainPopUpBehaviour1.setHideable(true);
            mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);
            SongPreviewController preview = getSongPreviewController();
            if (preview != null) {
                if (preview.isPlayingPreview())
                    preview.cancelPreview();
                else {
                    Artist artist = (Artist) songMenu;
                    ArrayList<Song> list = new ArrayList<>(artist.getSongs());
                    Collections.shuffle(list);
                    preview.previewSongs(list);
                }
            }
            Toasty.success(context, "Play Preview On...", Toasty.LENGTH_SHORT).show();
        }

        public void onPlaySong(View view) {
            mainPopUpBehaviour1.setHideable(true);
            mainPopUpBehaviour1.setState(BottomSheetBehavior.STATE_HIDDEN);
            Artist artist = (Artist) songMenu;
            MusicPlayerRemote.openAndShuffleQueue(artist.getSongs(), true);
        }

        public void onPlayNextPlayList(View view) {
            mainPopUpBehaviour2.setHideable(true);
            mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_HIDDEN);
            MusicPlayerRemote.playNext(new ArrayList<>(MusicUtil.getPlaylistSongList(MainActivity.this, (Playlist) songMenu)));
        }

        public void onEnqueuePlayList(View view) {
            mainPopUpBehaviour2.setHideable(true);
            mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_HIDDEN);
            MusicPlayerRemote.enqueue(new ArrayList<>(MusicUtil.getPlaylistSongList(MainActivity.this, (Playlist) songMenu)));
        }

        public void onAddToPlayList(View view) {
            mainPopUpBehaviour2.setHideable(true);
            mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_HIDDEN);
            addToPlayListDialog("PlayList");
        }

        public void onPlayListPreview(View view) {
            mainPopUpBehaviour2.setHideable(true);
            mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_HIDDEN);
            SongPreviewController preview = getSongPreviewController();
            if (preview != null) {
                if (preview.isPlayingPreview())
                    preview.cancelPreview();
                else {
                    ArrayList<Song> list = new ArrayList<>(MusicUtil.getPlaylistSongList(MainActivity.this, (Playlist) songMenu));
                    Collections.shuffle(list);
                    preview.previewSongs(list);
                }
            }
        }

        public void onRenamePlayList(View view) {
            mainPopUpBehaviour2.setHideable(true);
            mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_HIDDEN);
            addToPlayList("Rename");
        }

        public void onDeletePlayList(View view) {
            mainPopUpBehaviour2.setHideable(true);
            mainPopUpBehaviour2.setState(BottomSheetBehavior.STATE_HIDDEN);
            deleteSong(new ArrayList<>());
        }
    }
}