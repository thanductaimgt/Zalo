package com.mgt.zalo.ui.comment.reply

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.base.BaseView
import com.mgt.zalo.base.EmptyActivity
import com.mgt.zalo.data_model.Comment
import com.mgt.zalo.data_model.media.ImageMedia
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.data_model.media.VideoMedia
import com.mgt.zalo.data_model.react.React
import com.mgt.zalo.manager.ExternalIntentManager
import com.mgt.zalo.ui.comment.CommentAdapter
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.diff_callback.CommentDiffCallback
import kotlinx.android.synthetic.main.fragment_reply.*
import kotlinx.android.synthetic.main.item_comment.*
import kotlinx.android.synthetic.main.part_footer_comment_fragment.*
import kotlinx.android.synthetic.main.part_footer_comment_fragment.playIcon


class ReplyFragment: BaseFragment() {
    private val viewModel: ReplyViewModel by viewModels { viewModelFactory }

    private lateinit var replyAdapter: CommentAdapter

    lateinit var comment: Comment
    private var isReplying = false

    private var isCommentJustSentByMe = false

    override fun applyArguments(args: Bundle) {
        comment = args.getParcelable(ARG_1)!!
        isReplying = args.getBoolean(ARG_2)
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_reply, container,
                false)
    }

    override fun onBindViews() {
        replyAdapter = CommentAdapter(this, CommentDiffCallback())
        replyAdapter.isReply = true

        recyclerView.adapter = replyAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastVisiblePosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (viewModel.shouldLoadMoreComments() && lastVisiblePosition > replyAdapter.currentList.size - 5) {
                    viewModel.loadMoreReplies(replyAdapter.currentList.lastOrNull())
                }
            }
        })

        editText.addTextChangedListener {
            checkEnableSend()
        }
        descTextView.movementMethod

        if (isReplying) {
            updateReplyTo(comment)
            utils.showKeyboard(editText)
        }

        // bind comment to reply
        var commentViewHolder: CommentAdapter.CommentViewHolder? = null

        val commentEventListener = object : BaseOnEventListener {
            override fun onClick(view: View) {
                when (view.id) {
                    R.id.avatarImgView -> showCommentOwnerProfile(comment)
                    R.id.nameTextView -> showCommentOwnerProfile(comment)
                    R.id.reactLayout -> viewCommentReacts(comment)
                    R.id.imageView -> viewCommentMedia(comment)
                    R.id.reactTextView -> {
                        reactComment(comment)
                        commentViewHolder!!.bindMetrics(comment)
                    }
                    R.id.replyTextView -> updateReplyTo(comment)
                }
            }

            override fun onLongClick(view: View): Boolean {
                when(view.id){
                    R.id.rootItemView->{

                    }
                }
                return true
            }
        }

        commentViewHolder = CommentAdapter.CommentViewHolder(
                rootItemView,
                commentEventListener,
                sessionManager, utils, resourceManager, imageLoader
        )

        commentViewHolder.apply {
            bindOwner(comment)
            bindText(comment)
            bindMedia(comment)
            bindMetrics(comment)
            bindReplies(comment)
            bindOnClick()
            viewReplyTextView.visibility = View.GONE
        }

        headerLayout.setOnClickListener(this)
        sendImgView.setOnClickListener(this)
        uploadImageImgView.setOnClickListener(this)
        uploadVideoImgView.setOnClickListener(this)
        discardMediaImgView.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveIsLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                loadingAnimView.visibility = View.VISIBLE
            } else {
                loadingAnimView.visibility = View.GONE
            }
        })

        viewModel.liveReplies.observe(viewLifecycleOwner, Observer {
            replyAdapter.submitList(it) {
                if (isCommentJustSentByMe) {
                    recyclerView.scrollToPosition(replyAdapter.itemCount - 1)
                    isCommentJustSentByMe = false
                }
            }
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.sendImgView -> {
                viewModel.myReply.createdTime = System.currentTimeMillis()
                viewModel.myReply.text = editText.text.toString()
                viewModel.sendReply { isSuccess ->
                    if (!isSuccess) {
                        Toast.makeText(requireContext(), getString(R.string.label_error_occurred), Toast.LENGTH_SHORT).show()
                    } else {
                        comment.replyCount++
                    }
                }
                editText.setText("")
                viewModel.resetReply()
                updateSelectedMedia()
                isCommentJustSentByMe = true
            }
            R.id.uploadImageImgView -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_IMAGE_REQUEST, ExternalIntentManager.CHOOSER_TYPE_IMAGE, false)
            }
            R.id.uploadVideoImgView -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_VIDEO_REQUEST, ExternalIntentManager.CHOOSER_TYPE_VIDEO, false)
            }
            R.id.discardMediaImgView -> {
                viewModel.myReply.media = null
                updateSelectedMedia()
            }
            R.id.avatarImgView -> {
                onClickUser(view.parent as View)
            }
            R.id.nameTextView -> {
                onClickUser(view.parent.parent as View)
            }
            R.id.reactTextView -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val comment = replyAdapter.currentList[position]

                reactComment(comment)
                replyAdapter.notifyItemChanged(position, arrayListOf(Comment.PAYLOAD_METRICS))
            }
            R.id.imageView -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val comment = replyAdapter.currentList[position]

                viewCommentMedia(comment)
            }
            R.id.headerLayout -> {
                parent.onBackPressed()
            }
            R.id.replyTextView -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val comment = replyAdapter.currentList[position]

                updateReplyTo(comment)
            }
            R.id.reactLayout -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val comment = replyAdapter.currentList[position]

                viewCommentReacts(comment)
            }
        }
    }

    private fun updateReplyTo(comment: Comment){
        editText.setText("${comment.ownerName} ")
        editText.setSelection(editText.text.length)
    }

    private fun viewCommentReacts(comment: Comment) {
        fragmentManager().addReactFragment(comment.reacts)
    }

    private fun viewCommentMedia(comment: Comment) {
        startActivity(Intent(requireContext(), EmptyActivity::class.java).apply {
            putExtra(EmptyActivity.KEY_FRAGMENT, BaseView.FRAGMENT_MEDIA)
            putExtra(EmptyActivity.KEY_MEDIA, comment.media)
            putExtra(EmptyActivity.KEY_MEDIAS, arrayListOf(comment.media))
        })
    }

    private fun reactComment(comment: Comment) {
        val reactedType = React.TYPE_LOVE

        val curUser = sessionManager.curUser!!
        if (comment.reacts[curUser.id] == null) {
            database.reactComment(comment, reactedType)
            comment.reacts[curUser.id!!] = React(
                    ownerId = curUser.id,
                    ownerName = curUser.name,
                    ownerAvatarUrl = curUser.avatarUrl,
                    type = reactedType,
                    createdTime = System.currentTimeMillis()
            )
            comment.reactCount++
        } else {
            database.unReactComment(comment)
            comment.reacts.remove(curUser.id!!)
            comment.reactCount--
        }
    }

    private fun onClickUser(view: View) {
        val position = recyclerView.getChildAdapterPosition(view)
        val comment = replyAdapter.currentList[position]

        showCommentOwnerProfile(comment)
    }

    private fun showCommentOwnerProfile(comment: Comment) {
        startActivity(Intent(requireContext(), EmptyActivity::class.java).apply {
            putExtra(EmptyActivity.KEY_FRAGMENT, BaseView.FRAGMENT_PROFILE)
            putExtra(EmptyActivity.KEY_USER_ID, comment.ownerId)
        })
    }

    override fun onActivityResult(requestCode: Int, intent: Intent?) {
        utils.assertNotNull(intent, TAG, "onActivityResult.intent") { intentNotNull ->
            val uri = intentNotNull.data!!
            when (requestCode) {
                Constants.CHOOSE_IMAGE_REQUEST -> {
                    viewModel.createMedia(uri = uri.toString(), mediaType = Media.TYPE_IMAGE) {
                        viewModel.myReply.media = it
                        updateSelectedMedia()
                    }
                }
                Constants.CHOOSE_VIDEO_REQUEST -> {
                    viewModel.createMedia(uri = uri.toString(), mediaType = Media.TYPE_VIDEO) {
                        viewModel.myReply.media = it
                        updateSelectedMedia()
                    }
                }
            }
        }
    }

    private fun updateSelectedMedia() {
        if (viewModel.myReply.media != null) {
            val media = viewModel.myReply.media!!

            mediaPreviewImgView.layoutParams = mediaPreviewImgView.layoutParams.apply {
                this as ConstraintLayout.LayoutParams
                dimensionRatio = utils.getAdjustedRatio(media.ratio!!)
            }

            when (media) {
                is ImageMedia -> {
                    imageLoader.load(media.uri, mediaPreviewImgView) {
                        it.fit().centerCrop()
                    }
                    playIcon.visibility = View.GONE
                }
                is VideoMedia -> {
                    resourceManager.getVideoThumbUri(media.uri!!) { uri ->
                        imageLoader.load(uri, mediaPreviewImgView) {
                            it.fit().centerCrop()
                        }
                    }
                    playIcon.visibility = View.VISIBLE
                }
            }
            mediaPreviewImgView.visibility = View.VISIBLE
            discardMediaImgView.visibility = View.VISIBLE
        } else {
            mediaPreviewImgView.setImageDrawable(null)
            mediaPreviewImgView.visibility = View.GONE
            discardMediaImgView.visibility = View.GONE
        }

        checkEnableSend()
    }

    private fun checkEnableSend(){
        sendImgView.visibility = if(TextUtils.isEmpty(editText.text) && viewModel.myReply.media == null){
            View.GONE
        }else{
            View.VISIBLE
        }
    }
}