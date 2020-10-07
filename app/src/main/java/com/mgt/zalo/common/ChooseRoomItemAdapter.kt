package com.mgt.zalo.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.room.RoomItem
import com.mgt.zalo.util.diff_callback.RoomItemDiffCallback
import com.mgt.zalo.util.smartLoad
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_room.view.*
import javax.inject.Inject

class ChooseRoomItemAdapter @Inject constructor(
        private val eventListener: BaseOnEventListener,
        diffCallback: RoomItemDiffCallback
) : BaseListAdapter<RoomItem, ChooseRoomItemAdapter.UserIconViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserIconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_remove_user, parent, false)
        view.setOnClickListener(eventListener)
        return UserIconViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserIconViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class UserIconViewHolder(itemView: View) : BaseViewHolder(itemView){
        override fun bind(position: Int) {
            itemView.apply {
                Picasso.get()
                        .smartLoad(currentList[position].avatarUrl, resourceManager, watchOwnerAvatarImgView){
                            it.fit().centerCrop()
                        }
            }
        }
    }
}