package vng.zalo.tdtai.zalo.ui.home.contacts.official_account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_official_account.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.loadCompat
import javax.inject.Inject

class OfficialAccountSubFragmentAdapter @Inject constructor(
        private val clickListener: View.OnClickListener,
        diffCallback: RoomItemDiffCallback,
        private val resourceManager: ResourceManager
) : ZaloListAdapter<RoomItem, OfficialAccountSubFragmentAdapter.OfficialAccountViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfficialAccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_official_account, parent, false)
        view.setOnClickListener(clickListener)
        return OfficialAccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfficialAccountViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class OfficialAccountViewHolder(itemView: View) : BindableViewHolder(itemView) {

        override fun bind(position: Int) {
            itemView.apply {
                val roomItem = getItem(position)

                nameTextView.text = roomItem.getDisplayName(resourceManager)

                Picasso.Builder(avatarImgView.context)
                        .build().loadCompat(roomItem.avatarUrl, resourceManager)
                        .fit()
                        .centerCrop()
                        .into(avatarImgView)
            }
        }
    }
}