package vng.zalo.tdtai.zalo.zalo.shared_adapters

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
                val (_, avatar, lastMsg, lastMsgTime, name, unseenMsgNum) = getItem(position)

                nameTextView.text = name
                Utils.formatTextOnNumberOfLines(nameTextView, 1)

                recvTimeTextView.text = if (lastMsgTime != null) Utils.getTimeDiffOrFormatTime(lastMsgTime.toDate()) else ""

                descTextView.text = lastMsg
                Utils.formatTextOnNumberOfLines(descTextView, 1)

                Picasso.get()
                        .load(avatar)
                        .fit()
                        .into(avatarImgView)

                if (unseenMsgNum > 0) {
                    iconTextView.text = if (unseenMsgNum < Constants.MAX_UNSEEN_MSG_NUM) unseenMsgNum.toString() else Constants.MAX_UNSEEN_MSG_NUM.toString() + "+"
                    iconTextView.visibility = View.VISIBLE
                    nameTextView.setTypeface(null, Typeface.BOLD)
                    descTextView.setTypeface(null, Typeface.BOLD)
                    recvTimeTextView.setTypeface(null, Typeface.BOLD)
                } else {
                    iconTextView.visibility = View.GONE
                    nameTextView.setTypeface(null, Typeface.NORMAL)
                    descTextView.setTypeface(null, Typeface.NORMAL)
                    recvTimeTextView.setTypeface(null, Typeface.NORMAL)
                }
            }
        }
    }
    companion object {
        private val TAG = RoomItemAdapter::class.java.simpleName
    }
}