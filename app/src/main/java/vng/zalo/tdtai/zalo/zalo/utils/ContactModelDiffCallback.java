package vng.zalo.tdtai.zalo.zalo.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import javax.inject.Inject;

import vng.zalo.tdtai.zalo.zalo.models.RoomItem;

public class ContactModelDiffCallback extends DiffUtil.ItemCallback<RoomItem> {
    @Inject
    public ContactModelDiffCallback(){}

    @Override
    public boolean areItemsTheSame(@NonNull RoomItem oldItem, @NonNull RoomItem newItem) {
        return oldItem.roomId.equals(newItem.roomId);
    }

    @Override
    public boolean areContentsTheSame(@NonNull RoomItem oldItem, @NonNull RoomItem newItem) {
        return oldItem.equals(newItem);
    }
}