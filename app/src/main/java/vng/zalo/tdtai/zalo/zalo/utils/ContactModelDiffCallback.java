package vng.zalo.tdtai.zalo.zalo.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import javax.inject.Inject;

import vng.zalo.tdtai.zalo.zalo.models.ContactItemModel;

public class ContactModelDiffCallback extends DiffUtil.ItemCallback<ContactItemModel> {
    @Inject
    public ContactModelDiffCallback(){}

    @Override
    public boolean areItemsTheSame(@NonNull ContactItemModel oldItem, @NonNull ContactItemModel newItem) {
        return oldItem.phone.equals(newItem.phone);
    }

    @Override
    public boolean areContentsTheSame(@NonNull ContactItemModel oldItem, @NonNull ContactItemModel newItem) {
        return oldItem.equals(newItem);
    }
}