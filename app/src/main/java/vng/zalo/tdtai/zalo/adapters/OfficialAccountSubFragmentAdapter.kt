package vng.zalo.tdtai.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_official_account.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.views.fragments.OfficialAccountSubFragment

class OfficialAccountSubFragmentAdapter(private val officialAccountSubFragment: OfficialAccountSubFragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ZaloListAdapter<RoomItem, OfficialAccountSubFragmentAdapter.OfficialAccountViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfficialAccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_official_account, parent, false)
        return OfficialAccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfficialAccountViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class OfficialAccountViewHolder(itemView: View) : BindableViewHolder(itemView) {

        override fun bind(position: Int) {
            with(itemView) {
                setOnClickListener(officialAccountSubFragment)
                val roomItem = getItem(position)

                nameTextView.text = roomItem.getDisplayName()

                Picasso.Builder(avatarImgView.context)
                        .build().load(roomItem.avatarUrl)
                        .fit()
                        .centerInside()
                        .into(avatarImgView)
            }
        }
    }
}