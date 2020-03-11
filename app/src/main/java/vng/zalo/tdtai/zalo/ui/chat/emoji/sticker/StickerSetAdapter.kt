package vng.zalo.tdtai.zalo.ui.chat.emoji.sticker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_sticker.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.model.Sticker
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Inject
import javax.inject.Named

class StickerSetAdapter @Inject constructor(
        @Named(Constants.FRAGMENT_NAME) private val clickListener: View.OnClickListener
) : RecyclerView.Adapter<StickerSetAdapter.StickerSetViewHolder>() {
    var stickers: List<Sticker> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerSetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
        view.setOnClickListener(clickListener)
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