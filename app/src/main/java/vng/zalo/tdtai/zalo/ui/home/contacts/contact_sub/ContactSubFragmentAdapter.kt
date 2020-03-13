package vng.zalo.tdtai.zalo.ui.home.contacts.contact_sub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_contact_sub_fragment.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.smartLoad
import javax.inject.Inject

class ContactSubFragmentAdapter @Inject constructor(
        private val clickListener: View.OnClickListener,
        diffCallback: RoomItemDiffCallback,
        private val resourceManager: ResourceManager
) : ZaloListAdapter<RoomItem, ContactSubFragmentAdapter.ContactViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_sub_fragment, parent, false)
        view.setOnClickListener(clickListener)
        view.voiceCallImgView.setOnClickListener(clickListener)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ContactViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.apply {
                val roomItem = getItem(position)

                nameTextView.text = roomItem.getDisplayName(resourceManager)

                Picasso.get()
                        .smartLoad(roomItem.avatarUrl, resourceManager, avatarImgView) {
                            it.fit()
                                    .centerCrop()
                        }
            }
        }
    }
}