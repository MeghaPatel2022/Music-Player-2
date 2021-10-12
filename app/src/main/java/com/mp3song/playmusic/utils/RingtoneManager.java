package com.mp3song.playmusic.utils;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mp3song.playmusic.R;

public class RingtoneManager {

    public static Dialog dialog;

    public static boolean requiresDialog(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !Settings.System.canWrite(context);
        }
        return false;
    }

    public static Dialog showDialog(Context context) {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_create_playlist);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        EditText etPlayListName = dialog.findViewById(R.id.etPlayListName);
        View view1 = dialog.findViewById(R.id.view);
        TextView tvRingtoneTitle = dialog.findViewById(R.id.tvRingtoneTitle);
        View tvRingtoneDesc = dialog.findViewById(R.id.tvRingtoneDesc);

        etPlayListName.setVisibility(View.GONE);
        view1.setVisibility(View.GONE);
        tvRingtoneTitle.setVisibility(View.VISIBLE);
        tvRingtoneDesc.setVisibility(View.VISIBLE);

        dialog.findViewById(R.id.tvCreate).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
            if (dialog.isShowing())
                dialog.dismiss();
        });

        dialog.findViewById(R.id.tvCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        });

        dialog.show();

        return dialog;
    }

    public Uri setRingtone(@NonNull final Context context, final int id) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = MusicUtil.getSongFileUri(id);
        try {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
            resolver.update(uri, values, null, null);
        } catch (@NonNull Exception e) {
            e.printStackTrace();
            return uri;
        }

        try {
            try (Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.MediaColumns.TITLE},
                    BaseColumns._ID + "=?",
                    new String[]{String.valueOf(id)},
                    null)) {
                if (cursor != null && cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
                    final String message = context.getString(R.string.x_has_been_set_as_ringtone, cursor.getString(0));
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uri;
    }
}
