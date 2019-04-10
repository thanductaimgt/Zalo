package vng.zalo.tdtai.zalo.zalo.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import vng.zalo.tdtai.zalo.zalo.models.ChatItemModel;

public class ChatItemDiffCallback extends DiffUtil.ItemCallback<ChatItemModel> {
    private static final String TAG = ChatItemDiffCallback.class.getSimpleName();

//    @Inject
    public ChatItemDiffCallback(){}

    @Override
    public boolean areItemsTheSame(@NonNull ChatItemModel oldItem, @NonNull ChatItemModel newItem) {
        Log.d(TAG,"areItemsTheSame"+(oldItem.id == newItem.id));
        return oldItem.id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(@NonNull ChatItemModel oldItem, @NonNull ChatItemModel newItem) {
        Log.d(TAG,"areContentsTheSame"+oldItem.equals(newItem));
        return oldItem.equals(newItem);
    }
}