package com.mgt.zalo.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.RoomMember
import com.mgt.zalo.util.diff_callback.RoomMemberDiffCallback
import kotlinx.android.synthetic.main.item_small_user_icon.view.*

class RoomMemberAdapter(
        roomMemberDiffCallback: RoomMemberDiffCallback
) : BaseListAdapter<RoomMember, RoomMemberAdapter.UserIconViewHolder>(roomMemberDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserIconViewHolder {
        return UserIconViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_small_user_icon, parent, false))
    }

    override fun onBindViewHolder(holder: UserIconViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class UserIconViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            imageLoader.load(currentList[position].avatarUrl, itemView.userIconImgView) {
                it.placeholder(R.drawable.default_peer_avatar)
                        .fit()
                        .centerCrop()
            }
        }
    }
}