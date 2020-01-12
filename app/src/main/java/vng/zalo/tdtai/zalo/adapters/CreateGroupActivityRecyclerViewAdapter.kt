package vng.zalo.tdtai.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_room.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback

class CreateGroupActivityRecyclerViewAdapter(diffCallback: RoomItemDiffCallback) : ZaloListAdapter<RoomItem, CreateGroupActivityRecyclerViewAdapter.UserIconViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserIconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_icon, parent, false)
        return UserIconViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserIconViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class UserIconViewHolder(itemView: View) : BindableViewHolder(itemView){
        override fun bind(position: Int) {
            itemView.apply {
                Picasso.get()
                        .load(currentList[position].avatarUrl)
                        .fit()
                        .centerInside()
                        .into(avatarImgView)

                setOnClickListener(context as View.OnClickListener)
            }
        }
    }
}