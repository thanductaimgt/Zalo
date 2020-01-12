package vng.zalo.tdtai.zalo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.android.synthetic.main.item_sticker.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.Sticker
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder

class StickerSetFragmentAdapter(private val fragment: Fragment) : RecyclerView.Adapter<StickerSetFragmentAdapter.StickerSetViewHolder>() {
    var stickers: List<Sticker> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerSetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
        view.setOnClickListener(fragment as View.OnClickListener)
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