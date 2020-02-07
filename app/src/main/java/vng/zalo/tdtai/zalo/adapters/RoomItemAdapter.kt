package vng.zalo.tdtai.zalo.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_room.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ResourceManager
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.models.room.RoomItemPeer
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.utils.loadCompat

class RoomItemAdapter(private val fragment: Fragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ZaloListAdapter<RoomItem, RoomItemAdapter.RoomItemViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        view.setOnClickListener(fragment as View.OnClickListener)
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
                    RoomItem.PAYLOAD_SEEN_MESSAGE -> holder.bindSeenStatus(roomItem)
                    RoomItem.PAYLOAD_ONLINE_STATUS -> holder.bindOnlineStatus(roomItem)
                    RoomItem.PAYLOAD_TYPING -> holder.bindLastMessage(roomItem)
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    inner class RoomItemViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.apply {
                val roomItem = getItem(position)

                nameTextView.text = roomItem.getDisplayName()

                Picasso.get()
                        .loadCompat(roomItem.avatarUrl)
                        .placeholder(if (roomItem.roomType == Room.TYPE_PEER) R.drawable.default_peer_avatar else R.drawable.default_group_avatar)
                        .fit()
                        .centerCrop()
                        .into(avatarImgView)

                bindLastMessage(roomItem)
                bindSeenStatus(roomItem)
                bindOnlineStatus(roomItem)
            }
        }

        fun bindOnlineStatus(roomItem: RoomItem) {
            if (roomItem.roomType == Room.TYPE_PEER) {
                itemView.apply {
                    if ((roomItem as RoomItemPeer).lastOnlineTime == null) {
                        onlineStatusImgView.visibility = View.VISIBLE
                    } else {
                        onlineStatusImgView.visibility = View.GONE
                    }
                }
            }
        }

        fun bindLastMessage(roomItem: RoomItem) {
            itemView.apply {
                timeTextView.text = if (roomItem.lastMsgTime != null) Utils.getTimeDiffFormat(context, roomItem.lastMsgTime!!.toDate()) else ""

                descTextView.text = if (roomItem.lastTypingMember != null) {
                    typingAnimView.visibility = View.VISIBLE
                    typingAnimView.postDelayed({
                        typingAnimView.playAnimation()
                    }, 100)
                    descTextView.setTextColor(ContextCompat.getColor(context, R.color.lightPrimary))
                    String.format("%s: %s",
                            if (roomItem.lastTypingMember!!.phone == ZaloApplication.curUser!!.phone) {
                                context.getString(R.string.label_me)
                            } else {
                                Picasso.get()
                                        .loadCompat(roomItem.lastTypingMember!!.avatarUrl)
                                        .placeholder(R.drawable.default_peer_avatar)
                                        .fit()
                                        .centerCrop()
                                        .into(typingUserAvatarImgView)
                                typingUserAvatarImgView.visibility = View.VISIBLE

                                ResourceManager.getNameFromPhone(roomItem.lastTypingMember!!.phone!!)
                                        ?: roomItem.lastTypingMember!!.name
                            },
                            context.getString(R.string.description_typing)
                    )
                } else {
                    typingUserAvatarImgView.visibility = View.GONE

                    typingAnimView.visibility = View.GONE
                    descTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    if (roomItem.lastSenderPhone != null) {
                        val senderName =
                                if (roomItem.lastSenderPhone == ZaloApplication.curUser!!.phone)
                                    context.getString(R.string.label_me)
                                else
                                    ResourceManager.getNameFromPhone(roomItem.lastSenderPhone!!)
                                            ?: roomItem.lastSenderName

                        String.format("%s: %s", senderName, roomItem.lastMsg)
                    } else ""
                }
            }
        }

        fun bindSeenStatus(roomItem: RoomItem) {
            itemView.apply {
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
                    timeTextView.setTypeface(null, Typeface.NORMAL)
                } else {
                    iconTextView.visibility = View.VISIBLE
                    descTextView.setTypeface(null, Typeface.BOLD)
                    timeTextView.setTypeface(null, Typeface.BOLD)
                }
            }
        }
    }
}