package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.contacts_sub_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_contact_sub_fragment.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder
import vng.zalo.tdtai.zalo.zalo.utils.Utils

class AllContactAdapter(private val contactSubFragment: ContactSubFragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ListAdapter<RoomItem, AllContactAdapter.ContactViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_sub_fragment, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {
        override fun bind(position: Int) {
            itemView.setOnClickListener(contactSubFragment)
            val roomItem = getItem(position)

            itemView.phoneTextView.text = roomItem.name
            Utils.formatTextOnNumberOfLines(itemView.phoneTextView, 1)

            Picasso.get()
                    .load(roomItem.avatar)
                    .fit()
                    .into(itemView.avatarImgView)
        }
    }
}