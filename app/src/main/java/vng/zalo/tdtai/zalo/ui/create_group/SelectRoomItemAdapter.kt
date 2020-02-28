package vng.zalo.tdtai.zalo.ui.create_group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_select_contact.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.utils.loadCompat

class SelectRoomItemAdapter (
        diffCallback: RoomItemDiffCallback,
        private val shouldDisplayDesc: Boolean,
        private val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>>,
        private val utils: Utils,
        private val resourceManager: ResourceManager
) : ZaloListAdapter<RoomItem, SelectRoomItemAdapter.AllContactsViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllContactsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_contact, parent, false)
        return AllContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllContactsViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            (payloads[0] as ArrayList<*>).forEach {
                when (it) {
                    RoomItem.PAYLOAD_SELECT -> holder.bindSelectState(currentList[position])
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: AllContactsViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class AllContactsViewHolder(itemView: View) : BindableViewHolder(itemView) {

        override fun bind(position: Int) {
            itemView.apply {
                val roomItem = currentList[position]

                nameTextView.text = roomItem.getDisplayName(resourceManager)
                if (shouldDisplayDesc && roomItem.lastMsgTime != null) {
                    descTextView.text = utils.getTimeDiffFormat(context, roomItem.lastMsgTime!!)
                    descTextView.visibility = View.VISIBLE
                } else {
                    descTextView.visibility = View.GONE
                }

                Picasso.get()
                        .loadCompat(roomItem.avatarUrl, resourceManager)
                        .fit()
                        .centerCrop()
                        .into(avatarImgView)

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