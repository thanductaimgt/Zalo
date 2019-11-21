package vng.zalo.tdtai.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_select_contact.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder

class RecentContactsSubFragmentAdapter(private val fragment: Fragment) : RecyclerView.Adapter<RecentContactsSubFragmentAdapter.RecentContactsViewHolder>() {
    var roomItems: List<RoomItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentContactsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_contact, parent, false)
        return RecentContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentContactsViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return roomItems.size
    }

    inner class RecentContactsViewHolder(itemView: View) : BindableViewHolder(itemView) {

        override fun bind(position: Int) {
            itemView.apply {
                setOnClickListener(fragment as View.OnClickListener)
                val roomItem = roomItems[position]

                nameTextView.text = roomItem.name

                Picasso.get()
                        .load(roomItem.avatarUrl)
                        .fit()
                        .centerInside()
                        .into(avatarImgView)
            }
        }
    }
}