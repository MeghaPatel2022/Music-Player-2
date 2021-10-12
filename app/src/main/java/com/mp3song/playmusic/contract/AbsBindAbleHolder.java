package com.mp3song.playmusic.contract;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbsBindAbleHolder<I> extends RecyclerView.ViewHolder {

    public AbsBindAbleHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(I item) {
    }
}
