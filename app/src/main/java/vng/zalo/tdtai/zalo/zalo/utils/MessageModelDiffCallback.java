package vng.zalo.tdtai.zalo.zalo.utils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import vng.zalo.tdtai.zalo.zalo.models.Message;

public class MessageModelDiffCallback extends DiffUtil.ItemCallback<Message> {
    @Inject
    public MessageModelDiffCallback(){}

    @Override
    public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
        return oldItem.createdTime.equals(newItem.createdTime);
    }

    @Override
    public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
        return oldItem.equals(newItem);
    }
}