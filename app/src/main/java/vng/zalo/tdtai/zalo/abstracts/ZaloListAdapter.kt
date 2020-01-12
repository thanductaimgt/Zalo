package vng.zalo.tdtai.zalo.abstracts

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class ZaloListAdapter<T, VH : RecyclerView.ViewHolder?>(diffCallback: DiffUtil.ItemCallback<T>) : ListAdapter<T, VH>(diffCallback) {
    override fun submitList(list: List<T>?) {
        submitList(list, null)
    }

    override fun submitList(list: List<T>?, commitCallback: Runnable?) {
        super.submitList(
                if (list === currentList)
                    list.toMutableList()
                else
                    list
                , commitCallback)
    }
}