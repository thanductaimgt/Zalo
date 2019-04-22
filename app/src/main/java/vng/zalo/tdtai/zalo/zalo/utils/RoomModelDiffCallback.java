package vng.zalo.tdtai.zalo.zalo.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import vng.zalo.tdtai.zalo.zalo.models.RoomModel;

public class RoomModelDiffCallback extends DiffUtil.ItemCallback<RoomModel> {
    private static final String TAG = RoomModelDiffCallback.class.getSimpleName();

//    @Inject
    public RoomModelDiffCallback(){}

    @Override
    public boolean areItemsTheSame(@NonNull RoomModel oldItem, @NonNull RoomModel newItem) {
        return oldItem.id.equals(newItem.id);
    }

    @Override
    public boolean areContentsTheSame(@NonNull RoomModel oldItem, @NonNull RoomModel newItem) {
        Log.d(TAG,"areContentsTheSame"+oldItem.equals(newItem));
        return oldItem.equals(newItem);
    }
}