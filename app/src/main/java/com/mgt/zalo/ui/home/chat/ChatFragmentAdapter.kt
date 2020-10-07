package com.mgt.zalo.ui.home.chat

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.room.Room
import com.mgt.zalo.data_model.room.RoomItem
import com.mgt.zalo.data_model.room.RoomItemPeer
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.diff_callback.RoomItemDiffCallback
import kotlinx.android.synthetic.main.item_room.view.*
import javax.inject.Inject

class ChatFragmentAdapter @Inject constructor(
        private val eventListener: BaseOnEventListener,
        diffCallback: RoomItemDiffCallback) : BaseListAdapter<RoomItem, ChatFragmentAdapter.RoomItemViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        view.setOnClickListener(eventListener)
        return RoomItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onBindViewHolder(
            holder: RoomItemViewHolder,
            position: Int,
            payloads: ArrayList<*>
    ) {
        val roomItem = currentList[position]
        payloads.forEach {
            when (it) {
                RoomItem.PAYLOAD_NEW_MESSAGE -> holder.bindLastMessage(roomItem)
                RoomItem.PAYLOAD_SEEN_MESSAGE -> holder.bindSeenStatus(roomItem)
                RoomItem.PAYLOAD_ONLINE_STATUS -> holder.bindOnlineStatus(roomItem)
                RoomItem.PAYLOAD_TYPING -> holder.bindLastMessage(roomItem)
            }
        }
    }

    inner class RoomItemViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.apply {
                val roomItem = getItem(position)

                nameTextView.text = roomItem.getDisplayName(resourceManager)

                imageLoader.load(roomItem.avatarUrl, watchOwnerAvatarImgView) {
                            it.placeholder(if (roomItem.roomType == Room.TYPE_PEER) R.drawable.default_peer_avatar else R.drawable.default_group_avatar)
                                    .fit()
                                    .centerCrop()
                        }

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
                timeTextView.text = if (roomItem.lastMsgTime != null) utils.getTimeDiffFormat(roomItem.lastMsgTime!!) else ""

                descTextView.text = if (roomItem.lastTypingMember != null) {
                    typingAnimView.visibility = View.VISIBLE
                    typingAnimView.postDelayed({
                        typingAnimView.playAnimation()
                    }, 100)
                    descTextView.setTextColor(ContextCompat.getColor(context, R.color.lightPrimary))
                    String.format("%s: %s",
                            if (roomItem.lastTypingMember!!.userId == sessionManager.curUser!!.id) {
                                context.getString(R.string.label_me)
                            } else {
                                imageLoader.load(roomItem.lastTypingMember!!.avatarUrl, typingUserAvatarImgView) {
                                            it.placeholder(R.drawable.default_peer_avatar)
                                                    .fit()
                                                    .centerCrop()
                                        }

                                typingUserAvatarImgView.visibility = View.VISIBLE

                                resourceManager.getNameFromId(roomItem.lastTypingMember!!.userId!!)
                                        ?: roomItem.lastTypingMember!!.name
                            },
                            context.getString(R.string.description_typing)
                    )
                } else {
                    typingUserAvatarImgView.visibility = View.GONE

                    typingAnimView.visibility = View.GONE
                    descTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    if (roomItem.lastSenderId != null) {
                        val senderName =
                                if (roomItem.lastSenderId == sessionManager.curUser!!.id)
                                    context.getString(R.string.label_me)
                                else
                                    resourceManager.getNameFromId(roomItem.lastSenderId!!)
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