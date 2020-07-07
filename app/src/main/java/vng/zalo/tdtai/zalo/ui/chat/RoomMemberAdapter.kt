package vng.zalo.tdtai.zalo.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_small_user_icon.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseViewHolder
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.data_model.RoomMember
import vng.zalo.tdtai.zalo.util.RoomMemberDiffCallback
import vng.zalo.tdtai.zalo.util.smartLoad

class RoomMemberAdapter(
        private val resourceManager: ResourceManager,
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
            Picasso.get().smartLoad(currentList[position].avatarUrl, resourceManager, itemView.userIconImgView) {
                it.placeholder(R.drawable.default_peer_avatar)
                        .fit()
                        .centerCrop()
            }
        }
    }
}