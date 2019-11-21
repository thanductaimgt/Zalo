package vng.zalo.tdtai.zalo.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_room.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder

class CreateGroupActivityRecyclerViewAdapter(private val activity: Activity) : RecyclerView.Adapter<CreateGroupActivityRecyclerViewAdapter.UserIconViewHolder>() {
    var roomItems = ArrayList<RoomItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserIconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_icon, parent, false)
        return UserIconViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserIconViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return roomItems.size
    }

    inner class UserIconViewHolder(itemView: View) : BindableViewHolder(itemView){
        override fun bind(position: Int) {
            itemView.apply {
                setOnClickListener(activity as View.OnClickListener)

                Picasso.get()
                        .load(roomItems[position].avatarUrl)
                        .fit()
                        .centerInside()
                        .into(avatarImgView)
            }
        }
    }
}