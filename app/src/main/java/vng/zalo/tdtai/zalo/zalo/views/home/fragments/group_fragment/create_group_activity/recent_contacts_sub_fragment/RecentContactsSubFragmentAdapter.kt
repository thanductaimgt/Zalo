package vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.recent_contacts_sub_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_recent_contacts.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder
import vng.zalo.tdtai.zalo.zalo.utils.Utils

class RecentContactsSubFragmentAdapter(private val fragment: Fragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ListAdapter<RoomItem, RecentContactsSubFragmentAdapter.RecentContactsViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentContactsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_contacts, parent, false)
        return RecentContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentContactsViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class RecentContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {

        override fun bind(position: Int) {
            itemView.apply {
                setOnClickListener(fragment as View.OnClickListener)
                val roomItem = getItem(position)

                nameTextView.text = roomItem.name
                Utils.formatTextOnNumberOfLines(nameTextView, 1)

                Picasso.get()
                        .load(roomItem.avatar)
                        .fit()
                        .into(avatarImgView)
            }
        }
    }

    companion object {
        private val TAG = RecentContactsSubFragmentAdapter::class.java.simpleName
    }
}