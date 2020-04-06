package vng.zalo.tdtai.zalo.common

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_profile_media.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.util.ResourceDiffCallback
import vng.zalo.tdtai.zalo.util.Utils
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject

class MediaPreviewAdapter @Inject constructor(
        private val clickListener: View.OnClickListener,
        private val resourceManager: ResourceManager,
        private val utils: Utils,
        diffCallback: ResourceDiffCallback
) : BaseListAdapter<Media, MediaPreviewAdapter.DiaryViewHolder>(diffCallback) {
    var moreCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val holder = DiaryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_profile_media, parent, false))
        return holder.apply { bindOnClick() }
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int, payloads: ArrayList<*>) {
        payloads.forEach {
            when (it) {
                Media.PAYLOAD_DISPLAY_MORE -> holder.bindDisplayMore(position)
            }
        }
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class DiaryViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            when (val resource = currentList[position]) {
                is VideoMedia -> bindVideo(resource)
                is ImageMedia -> bindImage(resource)
            }

            bindDisplayMore(position)
        }

        private fun bindImage(imageMedia: ImageMedia) {
            itemView.apply {
                Picasso.get().smartLoad(imageMedia.uri, resourceManager, imageView) {
                    it.fit().centerCrop()
                }

                videoDurationTV.visibility = View.GONE
            }
        }

        private fun bindVideo(videoResource: VideoMedia) {
            itemView.apply {
                resourceManager.getVideoThumbUri(videoResource.uri) { uri ->
                    Picasso.get().smartLoad(uri, resourceManager, imageView) {
                        it.fit().centerCrop()
                    }
                }

                videoDurationTV.text = utils.getVideoDurationFormat(videoResource.duration)
                videoDurationTV.visibility = View.VISIBLE
            }
        }

        fun bindDisplayMore(position: Int) {
            itemView.apply {
                displayMoreTextView.visibility = if (moreCount > 0 && position == currentList.lastIndex) {
                    displayMoreTextView.text = "+$moreCount"
                    imageView.foreground = ColorDrawable(ContextCompat.getColor(context, R.color.blackTransparentStrong))
                    View.VISIBLE
                } else {
                    imageView.foreground = null
                    View.GONE
                }
            }
        }

        fun bindOnClick() {
            itemView.setOnClickListener(clickListener)
        }
    }
}