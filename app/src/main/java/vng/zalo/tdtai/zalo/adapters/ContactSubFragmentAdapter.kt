package vng.zalo.tdtai.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_contact_sub_fragment.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.views.fragments.ContactSubFragment

class ContactSubFragmentAdapter(private val contactSubFragment: ContactSubFragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ZaloListAdapter<RoomItem, ContactSubFragmentAdapter.ContactViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_sub_fragment, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ContactViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            with(itemView) {
                val roomItem = getItem(position)

                nameTextView.text = roomItem.getDisplayName()

                Picasso.get()
                        .load(roomItem.avatarUrl)
                        .fit()
                        .centerInside()
                        .into(avatarImgView)

                voiceCallImgView.setOnClickListener(contactSubFragment)
                setOnClickListener(contactSubFragment)
            }
        }
    }
}