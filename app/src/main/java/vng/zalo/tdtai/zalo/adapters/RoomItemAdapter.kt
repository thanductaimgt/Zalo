package vng.zalo.tdtai.zalo.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_room.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.utils.BindableViewHolder
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Utils

class RoomItemAdapter(private val fragment: Fragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ListAdapter<RoomItem, RoomItemAdapter.RoomItemViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return RoomItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onBindViewHolder(
            holder: RoomItemViewHolder,
            position: Int,
            payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            (payloads[0] as ArrayList<*>).forEach {
                val roomItem = currentList[position]
                when (it) {
                    RoomItem.PAYLOAD_NEW_MESSAGE -> holder.bindLastMessage(roomItem)
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }
//
//    override fun onViewRecycled(holder: RoomItemViewHolder) {
//        holder.itemView.apply {
//            descTextView.text = ""
//            recvMsgTimeTextView.text = ""
//            avatarImgView.setImageDrawable(null)
//            iconTextView.visibility = View.GONE
//        }
//    }

    inner class RoomItemViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.apply {
                setOnClickListener(fragment as View.OnClickListener)

                val roomItem = getItem(position)

                nameTextView.text = roomItem.name

                Picasso.get()
                        .load(roomItem.avatarUrl)
                        .error(if (roomItem.roomType == Constants.ROOM_TYPE_PEER) R.drawable.default_peer_avatar else R.drawable.default_group_avatar)
                        .fit()
                        .into(avatarImgView)

                bindLastMessage(roomItem)
            }
        }

        fun bindLastMessage(roomItem: RoomItem) {
            itemView.apply {
                recvMsgTimeTextView.text = if (roomItem.lastMsgTime != null) Utils.getTimeDiffFormat(context, roomItem.lastMsgTime!!.toDate()) else ""

                descTextView.text = if (roomItem.lastSenderPhone != null) {
                    val senderName =
                            if (roomItem.lastSenderPhone == ZaloApplication.currentUser!!.phone)
                                context.getString(R.string.label_me)
                            else
                                roomItem.lastSenderPhone

                    String.format("%s: %s", senderName, roomItem.lastMsg)
                } else ""

                if (roomItem.unseenMsgNum > 0) {
                    // if unseenMsgNum > N -> display: [N+]
                    iconTextView.text =
                            if (roomItem.unseenMsgNum < Constants.MAX_UNSEEN_MSG_NUM)
                                roomItem.unseenMsgNum.toString()
                            else
                                "${Constants.MAX_UNSEEN_MSG_NUM}+"

                    adjustViewsBasedOnSeenStatus(false)
                } else {
                    adjustViewsBasedOnSeenStatus(true)
                }
            }
        }

        private fun adjustViewsBasedOnSeenStatus(isSeen: Boolean) {
            itemView.apply {
                if (isSeen) {
                    iconTextView.visibility = View.GONE
                    descTextView.setTypeface(null, Typeface.NORMAL)
                    recvMsgTimeTextView.setTypeface(null, Typeface.NORMAL)
                } else {
                    iconTextView.visibility = View.VISIBLE
                    descTextView.setTypeface(null, Typeface.BOLD)
                    recvMsgTimeTextView.setTypeface(null, Typeface.BOLD)
                }
            }
        }
    }
}