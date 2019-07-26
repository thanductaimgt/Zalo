package vng.zalo.tdtai.zalo.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_official_account.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder
import vng.zalo.tdtai.zalo.zalo.views.fragments.OfficialAccountSubFragment

class OfficialAccountSubFragmentAdapter(private val officialAccountSubFragment: OfficialAccountSubFragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ListAdapter<RoomItem, OfficialAccountSubFragmentAdapter.OfficialAccountViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfficialAccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_official_account, parent, false)
        return OfficialAccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfficialAccountViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class OfficialAccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {

        override fun bind(position: Int) {
            with(itemView) {
                setOnClickListener(officialAccountSubFragment)
                val room = getItem(position)

                nameTextView.text = room.name

                Picasso.Builder(avatarImgView.context)
                        .build().load(room.avatarUrl)
                        .fit()
                        .into(avatarImgView)
            }
        }
    }
}