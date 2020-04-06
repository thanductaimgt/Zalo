package vng.zalo.tdtai.zalo.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_room.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.data_model.room.RoomItem
import vng.zalo.tdtai.zalo.util.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject

class ChooseRoomItemAdapter @Inject constructor(
        private val clickListener: View.OnClickListener,
        private val resourceManager: ResourceManager,
        diffCallback: RoomItemDiffCallback
) : BaseListAdapter<RoomItem, ChooseRoomItemAdapter.UserIconViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserIconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_remove_user, parent, false)
        view.setOnClickListener(clickListener)
        return UserIconViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserIconViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class UserIconViewHolder(itemView: View) : BindableViewHolder(itemView){
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