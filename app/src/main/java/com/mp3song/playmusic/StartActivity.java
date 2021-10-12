package com.mp3song.playmusic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.mp3song.playmusic.databinding.ActivityStartBinding;

public class StartActivity extends AppCompatActivity {

    private static final int CODE_PERMISSIONS_WRITE_STORAGE = 1;
    ActivityStartBinding startBinding;
    private PermissionListener mPermissionListener;

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

    public boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        startBinding = DataBindingUtil.setContentView(StartActivity.this, R.layout.activity_start);
        startBinding.imgStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkSelfPermission())
                    requestPermission();
                else {
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
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
                        Intent intent = new Intent(StartActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else onPermissionDenied();
                }
            }
        }
    }

    public interface PermissionListener {
        void onPermissionGranted();

        void onPermissionDenied();
    }
}