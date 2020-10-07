package com.mgt.zalo.ui.home.diary

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.common.MediaPreviewAdapter
import com.mgt.zalo.data_model.post.Diary
import com.mgt.zalo.data_model.post.Post
import com.mgt.zalo.util.diff_callback.DiaryDiffCallback
import com.mgt.zalo.util.diff_callback.MediaDiffCallback
import kotlinx.android.synthetic.main.item_diary.view.*
import kotlinx.android.synthetic.main.part_post_actions.view.*
import javax.inject.Inject


class DiaryAdapter @Inject constructor(
        private val eventListener: BaseOnEventListener,
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

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int, payloads: ArrayList<*>) {
        payloads.forEach {
            when (it) {
                Post.PAYLOAD_METRICS -> holder.bindMetrics(currentList[position])
            }
        }
    }

    inner class DiaryViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val diary = currentList[position]

            bindOwner(diary)
            bindText(diary)
            bindMedias(diary)
            bindMetrics(diary)
        }

        fun bindMetrics(diary: Diary) {
            itemView.apply {
                shareCountTextView.text = utils.getMetricFormat(diary.shareCount)
                reactCountTextView.text = utils.getMetricFormat(diary.reactCount)
                commentCountTextView.text = utils.getMetricFormat(diary.commentCount)

                if (diary.reacts[sessionManager.curUser!!.id!!] != null) {
                    reactImgView.setImageResource(R.drawable.heart2)
                    val redTint = ContextCompat.getColor(context, R.color.missedCall)
                    ImageViewCompat.setImageTintList(reactImgView, ColorStateList.valueOf(redTint))
                } else {
                    reactImgView.setImageResource(R.drawable.heart)
                    ImageViewCompat.setImageTintList(reactImgView, null)
                }
            }
        }

        private fun bindOwner(diary: Diary) {
            itemView.apply {
                imageLoader.load(diary.ownerAvatarUrl, avatarImgView) {
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
                val adapter = MediaPreviewAdapter(eventListener, diffCallback)
                adapter.doLooping = true
                mediaGridView.adapter = adapter
                adapter.submitListLimit(diary.medias)
            }
        }

        fun bindOnClick() {
            itemView.apply {
                avatarImgView.setOnClickListener(eventListener)
                nameTextView.setOnClickListener(eventListener)
                moreImgView.setOnClickListener(eventListener)
                shareImgView.setOnClickListener(eventListener)
                commentImgView.setOnClickListener(eventListener)
                reactImgView.setOnClickListener(eventListener)
                postActionsLayout.setOnClickListener(eventListener)
            }
        }
    }
}