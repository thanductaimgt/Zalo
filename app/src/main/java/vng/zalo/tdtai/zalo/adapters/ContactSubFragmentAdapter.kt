package vng.zalo.tdtai.zalo.adapters

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_contact_sub_fragment.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.utils.BindableViewHolder
import vng.zalo.tdtai.zalo.views.fragments.ContactSubFragment

class ContactSubFragmentAdapter(private val contactSubFragment: ContactSubFragment, diffCallback: DiffUtil.ItemCallback<RoomItem>) : ListAdapter<RoomItem, ContactSubFragmentAdapter.ContactViewHolder>(diffCallback) {

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
                setOnClickListener(contactSubFragment)
                val roomItem = getItem(position)

                phoneTextView.text = roomItem.name

                Picasso.get()
                        .load(roomItem.avatarUrl)
                        .fit()
                        .into(avatarImgView)

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    val outValue = TypedValue()
                    context!!.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                    callImgButton.setBackgroundResource(outValue.resourceId)
                    videoImgButton.setBackgroundResource(outValue.resourceId)
                }
            }
        }
    }
}