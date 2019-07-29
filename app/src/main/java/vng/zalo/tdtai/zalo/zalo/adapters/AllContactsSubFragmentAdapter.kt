package vng.zalo.tdtai.zalo.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_select_contact.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder
import vng.zalo.tdtai.zalo.zalo.utils.Utils

class AllContactsSubFragmentAdapter(private val fragment: Fragment) : RecyclerView.Adapter<AllContactsSubFragmentAdapter.AllContactsViewHolder>() {
    var roomItems:List<RoomItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllContactsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_contact, parent, false)
        return AllContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllContactsViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return roomItems.size
    }

    inner class AllContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {

        override fun bind(position: Int) {
            itemView.apply {
                setOnClickListener(fragment as View.OnClickListener)
                val roomItem = roomItems[position]

                nameTextView.text = roomItem.name
                Utils.formatTextOnNumberOfLines(nameTextView, 1)

                Picasso.get()
                        .load(roomItem.avatarUrl)
                        .fit()
                        .into(avatarImgView)
            }
        }
    }
}