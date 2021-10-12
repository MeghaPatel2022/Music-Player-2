package com.mp3song.playmusic.contract;

import android.widget.Filter;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import com.mp3song.playmusic.models.Song;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsDataAdapter<VH extends AbsBindAbleHolder, I> extends RecyclerView.Adapter<VH> implements Serializable, Filterable {
    private static final String TAG = "AbsDataAdapter";

    private final List<I> mFilterList = new ArrayList<>();
    private List<I> mData = new ArrayList<>();

    public final List<I> getData() {
        return mData;
    }

    public final void setData(List<I> data) {
        mData.clear();
        mFilterList.clear();

        if (data != null) {
            mFilterList.addAll(data);
            mData.addAll(data);
        }

        onDataSet();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    protected abstract void onDataSet();

    public void destroy() {
        mData.clear();
    }

    public I getDataItem(int i) {
        return mData.get(i);
    }

    public void addDataItem(int i, I item) {
        if (item != null) {
            int pos = (i < 0) ? 0 : (i >= mData.size()) ? mData.size() : i;
            mData.add(pos, item);
            notifyItemChanged(pos);
        }
    }

    public void addItem(I item) {
        if (item != null) {
            mData.add(item);
            notifyItemChanged(mData.size() - 1);
        }
    }

    public void removeSongAt(int i) {
        mData.remove(i);
        notifyItemRemoved(i);
    }

    protected int getMediaHolderPosition(int dataPosition) {
        return dataPosition;
    }

    protected int getDataPosition(int itemHolderPosition) {
        return itemHolderPosition;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mData = mFilterList;
                } else {
                    ArrayList<I> filteredList = new ArrayList<>();
                    for (I row : mData) {
                        String foldername = ((Song) row).title;
                        if (foldername.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    mData = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mData;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mData = (List<I>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
