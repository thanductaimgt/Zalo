package vng.zalo.tdtai.zalo.zalo.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_room.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder
import vng.zalo.tdtai.zalo.zalo.utils.Utils

class RoomItemAdapter(private val fragment: Fragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ListAdapter<RoomItem, RoomItemAdapter.RoomItemViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return RoomItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class RoomItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
        override fun bind(position: Int) {
            itemView.apply {
                setOnClickListener(fragment as View.OnClickListener)

                val roomItem = getItem(position)

                nameTextView.text = roomItem.name
                Utils.formatTextOnNumberOfLines(nameTextView, 1)

                recvMsgTimeTextView.text = if (roomItem.lastMsgTime != null) Utils.getTimeDiffOrFormatTime(roomItem.lastMsgTime!!.toDate()) else ""

                if (roomItem.lastSenderPhone != null) {
                    descTextView.text = String.format("%s: %s", if (roomItem.lastSenderPhone == ZaloApplication.currentUser!!.phone) fragment.getString(R.string.label_me) else roomItem.lastSenderPhone, roomItem.lastMsg)
                } else {
                    descTextView.text = ""
                }
                Utils.formatTextOnNumberOfLines(descTextView, 1)

                Picasso.get()
                        .load(roomItem.avatarUrl)
                        .fit()
                        .into(avatarImgView)

                if (roomItem.unseenMsgNum > 0) {
                    // if unseenMsgNum > N -> display: [N+]
                    iconTextView.text = if (roomItem.unseenMsgNum < Constants.MAX_UNSEEN_MSG_NUM) roomItem.unseenMsgNum.toString() else Constants.MAX_UNSEEN_MSG_NUM.toString() + "+"

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
                    nameTextView.setTypeface(null, Typeface.NORMAL)
                    descTextView.setTypeface(null, Typeface.NORMAL)
                    recvMsgTimeTextView.setTypeface(null, Typeface.NORMAL)
                } else {
                    iconTextView.visibility = View.VISIBLE
                    nameTextView.setTypeface(null, Typeface.BOLD)
                    descTextView.setTypeface(null, Typeface.BOLD)
                    recvMsgTimeTextView.setTypeface(null, Typeface.BOLD)
                }
            }
        }
    }
}