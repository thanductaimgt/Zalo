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
import vng.zalo.tdtai.zalo.zalo.models.Sticker
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder

class StickerSetFragmentAdapter (private val fragment: Fragment) : RecyclerView.Adapter<StickerSetFragmentAdapter.StickerSetViewHolder>() {
    var stickers:List<Sticker> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerSetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
        return StickerSetViewHolder(view)
    }

    override fun onBindViewHolder(holder: StickerSetViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return stickers.size
    }

    inner class StickerSetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ModelViewHolder {

        override fun bind(position: Int) {
            with(itemView){
                setOnClickListener(fragment as View.OnClickListener)

                animView.apply {
                    setAnimationFromUrl(stickers[position].url)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    scale = 0.1f
                    enableMergePathsForKitKatAndAbove(true)
                }
//                val url = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/user_avatars%2F0123456789.jpg?alt=media&token=2974774b-3312-4eb1-b565-0498cd6d0d90"
//                Picasso.get().load(url).fit().into(animView)

                Log.d(TAG,"bind.${stickers[position]}")
            }
        }
    }

    companion object{
        private val TAG = StickerSetFragmentAdapter::class.java.simpleName
    }
}