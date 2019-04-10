package vng.zalo.tdtai.zalo.zalo.utils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import vng.zalo.tdtai.zalo.zalo.models.MessageModel;

public class MessageModelDiffCallback extends DiffUtil.ItemCallback<MessageModel> {
    @Inject
    public MessageModelDiffCallback(){}

    @Override
    public boolean areItemsTheSame(@NonNull MessageModel oldItem, @NonNull MessageModel newItem) {
        return oldItem.msgId == newItem.msgId;
    }

    @Override
    public boolean areContentsTheSame(@NonNull MessageModel oldItem, @NonNull MessageModel newItem) {
        return oldItem.equals(newItem);
    }
}
