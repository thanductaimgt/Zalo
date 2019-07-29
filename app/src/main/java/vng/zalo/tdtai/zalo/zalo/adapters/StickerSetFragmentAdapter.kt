package vng.zalo.tdtai.zalo.zalo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_sticker.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder

class StickerSetFragmentAdapter (private val fragment: Fragment) : RecyclerView.Adapter<StickerSetFragmentAdapter.StickerSetViewHolder>() {
    var stickerLinks:List<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerSetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
        return StickerSetViewHolder(view)
    }

    override fun onBindViewHolder(holder: StickerSetViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return stickerLinks.size
    }

    inner class StickerSetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {

        override fun bind(position: Int) {
            with(itemView){
                setOnClickListener(fragment as View.OnClickListener)
                animView.setAnimationFromUrl(stickerLinks[position])
                animView.scale = 0.05f
                animView.scaleType = ImageView.ScaleType.CENTER_CROP
                Log.d("StickerSetVH.bind","animView.setAnimationFromUrl: ${stickerLinks[position]}")
            }
        }
    }
}