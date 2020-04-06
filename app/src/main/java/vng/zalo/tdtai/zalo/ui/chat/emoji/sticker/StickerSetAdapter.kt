package vng.zalo.tdtai.zalo.ui.chat.emoji.sticker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_sticker.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.data_model.Sticker
import javax.inject.Inject

class StickerSetAdapter @Inject constructor(
       private val stickerSetFragment: StickerSetFragment
) : RecyclerView.Adapter<StickerSetAdapter.StickerSetViewHolder>() {
    var stickers: List<Sticker> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerSetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
        view.setOnClickListener(stickerSetFragment)
        return StickerSetViewHolder(view)
    }

    override fun onBindViewHolder(holder: StickerSetViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return stickers.size
    }

    inner class StickerSetViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.apply {
                animView.setAnimationFromUrl(stickers[position].url)
//                LottieCompositionFactory.fromUrl(context, stickers[position].url).addListener {
//                    animView.setComposition(it)
//                    animView.playAnimation()
//                }
            }
        }
    }
}