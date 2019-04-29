package vng.zalo.tdtai.zalo.zalo.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;

public class RoomItemDiffCallback extends DiffUtil.ItemCallback<RoomItem> {
    private static final String TAG = RoomItemDiffCallback.class.getSimpleName();

//    @Inject
    public RoomItemDiffCallback(){}

    @Override
    public boolean areItemsTheSame(@NonNull RoomItem oldItem, @NonNull RoomItem newItem) {
        return oldItem.roomId.equals(newItem.roomId);
    }

    @Override
    public boolean areContentsTheSame(@NonNull RoomItem oldItem, @NonNull RoomItem newItem) {
        return oldItem.equals(newItem);
    }
}