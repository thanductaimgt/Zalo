package vng.zalo.tdtai.zalo.ui.home.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_diary.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.util.DiaryDiffCallback
import vng.zalo.tdtai.zalo.util.Utils
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject


class DiaryAdapter @Inject constructor(
        private val clickListener: View.OnClickListener,
        private val resourceManager: ResourceManager,
        private val utils: Utils,
        diffCallback: DiaryDiffCallback
) : BaseListAdapter<Diary, DiaryAdapter.DiaryViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val holder = DiaryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false))
        return holder.apply { bindOnClick() }
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class DiaryViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            val diary = currentList[position]

            bindOwner(diary)
            bindText(diary)
            bindImages(diary)
            bindVideos(diary)
            bindMetrics(diary)
        }

        private fun bindMetrics(diary: Diary) {
            itemView.apply {
                shareNumTextView.text = utils.getMetricFormat(diary.shareNumCount)
                reactCountTextView.text = utils.getMetricFormat(diary.reactCount)
                commentNumTextView.text = utils.getMetricFormat(diary.commentCount)
            }
        }

        private fun bindOwner(diary: Diary) {
            itemView.apply {
                Picasso.get().smartLoad(diary.ownerAvatarUrl, resourceManager, avatarImgView){
                    it.fit().centerCrop()
                }

                timeTextView.text = utils.getTimeDiffFormat(diary.createdTime)
                nameTextView.text = diary.ownerName
            }
        }

        private fun bindText(diary: Diary) {
            itemView.apply {
                descTextView.visibility = if (diary.text.isEmpty()) {
                    View.GONE
                } else {
                    descTextView.text = diary.text
                    View.VISIBLE
                }
            }
        }

        private fun bindImages(diary: Diary) {
            itemView.apply {
                imageView.visibility = diary.imagesUrl.firstOrNull()?.let { imageUrl ->
                    Picasso.get().smartLoad(imageUrl, resourceManager, imageView) {
                        it.fit().centerCrop()
                    }

                    View.VISIBLE
                } ?: View.GONE
            }
        }

        private fun bindVideos(diary: Diary) {

        }

        fun bindOnClick() {
            itemView.apply {
                imageView.setOnClickListener(clickListener)
                playerView.setOnClickListener(clickListener)
                avatarImgView.setOnClickListener(clickListener)
                nameTextView.setOnClickListener(clickListener)
                moreImgView.setOnClickListener(clickListener)
            }
        }
    }
}