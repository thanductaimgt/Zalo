package com.mgt.zalo.ui.story.story_detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_react.view.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.react.React
import com.mgt.zalo.manager.ResourceManager
import com.mgt.zalo.util.ReactDiffCallback
import com.mgt.zalo.util.smartLoad
import javax.inject.Inject

class ReactAdapter @Inject constructor(
        private val eventListener: BaseOnEventListener,
        private val resourceManager: ResourceManager,
        diffCallback: ReactDiffCallback
) : BaseListAdapter<React, ReactAdapter.ReactViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_react, parent, false)
        view.setOnClickListener(eventListener)
        return ReactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReactViewHolder, position: Int) {
        holder.bind(position)
    }

//    override fun onBindViewHolder(
//            holder: ReactViewHolder,
//            position: Int,
//            payloads: ArrayList<*>
//    ) {
//        val roomItem = currentList[position]
//        payloads.forEach {
//            when (it) {
//                RoomItem.PAYLOAD_NEW_MESSAGE -> holder.bindLastMessage(roomItem)
//                RoomItem.PAYLOAD_SEEN_MESSAGE -> holder.bindSeenStatus(roomItem)
//                RoomItem.PAYLOAD_ONLINE_STATUS -> holder.bindOnlineStatus(roomItem)
//                RoomItem.PAYLOAD_TYPING -> holder.bindLastMessage(roomItem)
//            }
//        }
//    }

    inner class ReactViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.apply {
                val react = getItem(position)

                nameTextView.text = react.ownerName
                Picasso.get().smartLoad(react.ownerAvatarUrl, resourceManager, avatarImgView){
                    it.fit().centerCrop()
                }
            }
        }
    }
}