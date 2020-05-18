package vng.zalo.tdtai.zalo.ui.home.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_diary.view.*
import kotlinx.android.synthetic.main.part_post_actions.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.common.MediaPreviewAdapter
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.manager.PlaybackManager
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.util.DiaryDiffCallback
import vng.zalo.tdtai.zalo.util.MediaDiffCallback
import vng.zalo.tdtai.zalo.util.Utils
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject


class DiaryAdapter @Inject constructor(
        private val clickListener: View.OnClickListener,
        private val resourceManager: ResourceManager,
        private val utils: Utils,
        private val playbackManager: PlaybackManager,
        diffCallback: DiaryDiffCallback
) : BaseListAdapter<Diary, DiaryAdapter.DiaryViewHolder>(diffCallback) {
    private val diffCallback = MediaDiffCallback()

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
            bindMedias(diary)
            bindMetrics(diary)
        }

        private fun bindMetrics(diary: Diary) {
            itemView.apply {
                shareCountTextView.text = utils.getMetricFormat(diary.shareCount)
                reactCountTextView.text = utils.getMetricFormat(diary.reactCount)
                commentCountTextView.text = utils.getMetricFormat(diary.commentCount)
            }
        }

        private fun bindOwner(diary: Diary) {
            itemView.apply {
                Picasso.get().smartLoad(diary.ownerAvatarUrl, resourceManager, avatarImgView){
                    it.fit().centerCrop()
                }

                timeTextView.text = utils.getTimeDiffFormat(diary.createdTime!!)
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

        private fun bindMedias(diary: Diary) {
            itemView.apply {
                val adapter = MediaPreviewAdapter(clickListener, resourceManager, utils, playbackManager, diffCallback)
                mediaGridView.adapter = adapter
                adapter.submitListLimit(diary.medias)
            }
        }

        fun bindOnClick() {
            itemView.apply {
                avatarImgView.setOnClickListener(clickListener)
                nameTextView.setOnClickListener(clickListener)
                moreImgView.setOnClickListener(clickListener)
            }
        }
    }
}