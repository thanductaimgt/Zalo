package vng.zalo.tdtai.zalo.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_small_user_icon.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.RoomMember
import vng.zalo.tdtai.zalo.utils.RoomMemberDiffCallback
import vng.zalo.tdtai.zalo.utils.loadCompat
import javax.inject.Inject

class RoomMemberAdapter (
        roomMemberDiffCallback: RoomMemberDiffCallback,
        private val resourceManager: ResourceManager
) : ZaloListAdapter<RoomMember, RoomMemberAdapter.UserIconViewHolder>(roomMemberDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserIconViewHolder {
        return UserIconViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_small_user_icon, parent, false))
    }

    override fun onBindViewHolder(holder: UserIconViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class UserIconViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            Picasso.get().loadCompat(currentList[position].avatarUrl, resourceManager)
                    .placeholder(R.drawable.default_peer_avatar)
                    .fit()
                    .centerCrop()
                    .into(itemView.userIconImgView)
        }
    }
}