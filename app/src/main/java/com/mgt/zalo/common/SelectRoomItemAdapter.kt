package com.mgt.zalo.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.room.RoomItem
import com.mgt.zalo.util.diff_callback.RoomItemDiffCallback
import com.mgt.zalo.util.smartLoad
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_select_contact.view.*

class SelectRoomItemAdapter(
        diffCallback: RoomItemDiffCallback,
        private val shouldDisplayDesc: Boolean,
        private val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>>
) : BaseListAdapter<RoomItem, SelectRoomItemAdapter.AllContactsViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllContactsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_contact, parent, false)
        return AllContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllContactsViewHolder, position: Int, payloads: ArrayList<*>) {
        payloads.forEach {
            when (it) {
                RoomItem.PAYLOAD_SELECT -> holder.bindSelectState(currentList[position])
            }
        }
    }

    override fun onBindViewHolder(holder: AllContactsViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class AllContactsViewHolder(itemView: View) : BaseViewHolder(itemView) {

        override fun bind(position: Int) {
            itemView.apply {
                val roomItem = currentList[position]

                nameTextView.text = roomItem.getDisplayName(resourceManager)
                if (shouldDisplayDesc && roomItem.lastMsgTime != null) {
                    descTextView.text = utils.getTimeDiffFormat(roomItem.lastMsgTime!!)
                    descTextView.visibility = View.VISIBLE
                } else {
                    descTextView.visibility = View.GONE
                }

                Picasso.get()
                        .smartLoad(roomItem.avatarUrl, resourceManager, watchOwnerAvatarImgView) {
                            it.fit()
                                    .centerCrop()
                        }

                bindSelectState(roomItem)

                setOnClickListener { onItemClicked(roomItem) }
            }
        }

        fun bindSelectState(roomItem: RoomItem) {
            itemView.apply {
                val isSelected = liveSelectedRoomItems.value!!.any { roomItem.roomId == it.roomId }
                if (radioButton.isChecked != isSelected) {
                    radioButton.isChecked = isSelected
                }
            }
        }
    }

    override fun submitList(list: List<RoomItem>?, commitCallback: Runnable?) {
        if (currentList.isEmpty()) {
            super.submitList(list, commitCallback)
        } else {
            notifyItemRangeChanged(0, currentList.size, arrayListOf(RoomItem.PAYLOAD_SELECT))
        }
    }

    fun onItemClicked(roomItem: RoomItem) {
        if (liveSelectedRoomItems.value!!.any { roomItem.roomId == it.roomId })
            liveSelectedRoomItems.value = liveSelectedRoomItems.value!!.apply { remove(roomItem) }
        else
            liveSelectedRoomItems.value = liveSelectedRoomItems.value!!.apply { add(roomItem) }
    }
}