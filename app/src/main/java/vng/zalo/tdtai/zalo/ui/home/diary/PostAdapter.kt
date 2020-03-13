package vng.zalo.tdtai.zalo.ui.home.diary

import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_post.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.post.Post
import vng.zalo.tdtai.zalo.utils.PostDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.utils.smartLoad
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


class PostAdapter @Inject constructor(
        private val resourceManager: ResourceManager,
        private val utils: Utils,
        diffCallback: PostDiffCallback
) : ZaloListAdapter<Post, PostAdapter.PostViewHolder>(diffCallback) {
    inner class PostViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            val post = currentList[position]

            bindOwner(post)
            bindText(post)
            bindImages(post)
            bindVideos(post)
            bindMetrics(post)
        }

        private fun bindMetrics(post: Post) {
            itemView.apply {
                shareNumTextView.text = utils.getMetricFormat(post.shareNum!!)
                emojiNumTextView.text = utils.getMetricFormat(post.emojiNum!!)
                commentNumTextView.text = utils.getMetricFormat(post.commentNum!!)
            }
        }

        private fun bindOwner(post: Post) {
            itemView.apply {
                Picasso.get().smartLoad(post.ownerAvatarUrl, resourceManager, avatarImgView) {
                    it.fit().centerCrop()
                }

                timeTextView.text = utils.getTimeDiffFormat(post.createdTime!!)
                nameTextView.text = post.ownerName
            }
        }

        private fun bindText(post: Post) {
            itemView.descTextView.visibility = if (post.description == null) {
                View.GONE
            } else {
                val formattedText = post.description!!.replace("\n", "\n\n")
                val spannableString = SpannableString(formattedText)

                val matcher: Matcher = Pattern.compile("\n\n").matcher(formattedText)
                while (matcher.find()) {
                    spannableString.setSpan(AbsoluteSizeSpan(20, true), matcher.start() + 1, matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                itemView.descTextView.text = spannableString

                View.VISIBLE
            }
        }

        private fun bindImages(post: Post) {
            itemView.apply {
                imageView.visibility = post.imagesUrl?.firstOrNull()?.let { imageUrl ->
                    Picasso.get().smartLoad(imageUrl, resourceManager, imageView) {
                        it.fit().centerCrop()
                    }

                    View.VISIBLE
                } ?: View.GONE
            }
        }

        private fun bindVideos(post: Post) {

        }

        fun bindOnClick() {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val holder = PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false))
        return holder.apply { bindOnClick() }
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(position)
    }
}