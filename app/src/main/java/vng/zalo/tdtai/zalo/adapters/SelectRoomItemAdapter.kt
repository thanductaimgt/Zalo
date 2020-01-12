package vng.zalo.tdtai.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_select_contact.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.views.activities.CreateGroupActivity

class SelectRoomItemAdapter(diffCallback: RoomItemDiffCallback, private val shouldDisplayDesc: Boolean, private val fragment:Fragment) : ZaloListAdapter<RoomItem, SelectRoomItemAdapter.AllContactsViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllContactsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_contact, parent, false)
        return AllContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllContactsViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class AllContactsViewHolder(itemView: View) : BindableViewHolder(itemView) {

        override fun bind(position: Int) {
            itemView.apply {
                val createGroupActivity = context as CreateGroupActivity
                setOnClickListener(fragment as View.OnClickListener)
                val roomItem = currentList[position]

                nameTextView.text = roomItem.getDisplayName()
                if (shouldDisplayDesc && roomItem.lastMsgTime != null) {
                    descTextView.text = Utils.getTimeDiffFormat(context, roomItem.lastMsgTime!!.toDate())
                    descTextView.visibility = View.VISIBLE
                } else {
                    descTextView.visibility = View.GONE
                }

                Picasso.get()
                        .load(roomItem.avatarUrl)
                        .fit()
                        .centerInside()
                        .into(avatarImgView)

                radioButton.isChecked = createGroupActivity.viewModel.liveSelectedRoomItems.value!!.contains(roomItem)
            }
        }
    }
}