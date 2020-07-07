package vng.zalo.tdtai.zalo.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

abstract class BaseListAdapter<T, VH : BaseViewHolder>(diffCallback: DiffUtil.ItemCallback<T>) : ListAdapter<T, VH>(diffCallback) {
    override fun submitList(list: List<T>?) {
        submitList(list, null)
    }

    override fun submitList(list: List<T>?, commitCallback: Runnable?) {
        super.submitList(list?.toMutableList(), commitCallback)
    }

    final override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        try {
            if (payloads.isNotEmpty()) {
                onBindViewHolder(holder, position, payloads[0] as ArrayList<*>)
            } else {
                onBindViewHolder(holder, position)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    open fun onBindViewHolder(holder: VH, position: Int, payloads: ArrayList<*>){}

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(position)
    }
}