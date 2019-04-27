package vng.zalo.tdtai.zalo.zalo.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;

public class RoomModelDiffCallback extends DiffUtil.ItemCallback<RoomItem> {
    private static final String TAG = RoomModelDiffCallback.class.getSimpleName();

//    @Inject
    public RoomModelDiffCallback(){}

    @Override
    public boolean areItemsTheSame(@NonNull RoomItem oldItem, @NonNull RoomItem newItem) {
        return oldItem.id.equals(newItem.id);
    }

    @Override
    public boolean areContentsTheSame(@NonNull RoomItem oldItem, @NonNull RoomItem newItem) {
        Log.d(TAG,"areContentsTheSame"+oldItem.equals(newItem));
        return oldItem.equals(newItem);
    }
}