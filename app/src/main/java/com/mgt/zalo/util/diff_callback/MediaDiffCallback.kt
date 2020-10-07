package com.mgt.zalo.util.diff_callback

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.mgt.zalo.data_model.media.Media
import javax.inject.Inject

class MediaDiffCallback @Inject constructor() : DiffUtil.ItemCallback<Media>() {
    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem.uri == newItem.uri
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem == newItem
    }
}