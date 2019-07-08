package vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.all_contacts_sub_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_select_contact.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder
import vng.zalo.tdtai.zalo.zalo.utils.Utils

class AllContactsSubFragmentAdapter(private val fragment: Fragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ListAdapter<RoomItem, AllContactsSubFragmentAdapter.AllContactsViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllContactsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_contact, parent, false)
        return AllContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllContactsViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class AllContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {

        override fun bind(position: Int) {
            itemView.apply {
                setOnClickListener(fragment as View.OnClickListener)
                val roomItem = getItem(position)

                groupNameEditText.text = roomItem.name
                Utils.formatTextOnNumberOfLines(groupNameEditText, 1)

                Picasso.get()
                        .load(roomItem.avatar)
                        .fit()
                        .into(avatarImgView)
            }
        }
    }
}