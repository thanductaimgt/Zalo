package com.mgt.zalo.ui.comment

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.bottom_sheet_comment_actions.*
import kotlinx.android.synthetic.main.bottom_sheet_comment_actions.view.*
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.part_footer_comment_fragment.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseBottomSheetFragment
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.base.BaseView
import com.mgt.zalo.base.EmptyActivity
import com.mgt.zalo.data_model.Comment
import com.mgt.zalo.data_model.media.ImageMedia
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.data_model.media.VideoMedia
import com.mgt.zalo.data_model.post.Post
import com.mgt.zalo.data_model.react.React
import com.mgt.zalo.manager.ExternalIntentManager
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.smartLoad
import javax.inject.Inject


class CommentFragment : BaseBottomSheetFragment() {
    private val viewModel: CommentViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var commentAdapter: CommentAdapter
    lateinit var post: Post

    private var viewCommentPosition: Int? = null
    private var longClickPosition: Int? = null

    lateinit var bottomSheetDialog: BottomSheetDialog

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_comment, container,
                false)
    }

    override fun onBindViews() {
        recyclerView.adapter = commentAdapter

        reactCountTextView.text = utils.getMetricFormat(post.reactCount)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastVisiblePosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (viewModel.shouldLoadMoreComments() && lastVisiblePosition > commentAdapter.currentList.size - 5) {
                    viewModel.loadMoreComments()
                }
            }
        })

        editText.addTextChangedListener {
            checkEnableSend()
        }

        if (post.reacts[sessionManager.curUser!!.id] != null) {
            reactImgView.setImageResource(R.drawable.heart2)
            val redTint = ContextCompat.getColor(requireContext(), R.color.missedCall)
            ImageViewCompat.setImageTintList(reactImgView, ColorStateList.valueOf(redTint))
        } else {
            reactImgView.setImageResource(R.drawable.heart)
            ImageViewCompat.setImageTintList(reactImgView, null)
        }

        sendImgView.setOnClickListener(this)
        uploadImageImgView.setOnClickListener(this)
        uploadVideoImgView.setOnClickListener(this)
        discardMediaImgView.setOnClickListener(this)
        headerLayout.setOnClickListener(this)
        reactImgView.setOnClickListener(this)
        closeImgView.setOnClickListener(this)
        mediaPreviewImgView.setOnClickListener(this)

        initBottomSheet()
    }

    private fun initBottomSheet() {
        val bottomSheetEventListener = object : BaseOnEventListener {
            override fun onClick(view: View) {
                val comment = commentAdapter.currentList[longClickPosition!!]
                when (view.id) {
                    R.id.replyTextView -> {
                        zaloFragmentManager.addReplyFragment(comment, true)
                    }
                    R.id.copyTextView -> {

                    }
                    R.id.deleteTextView -> {
                        storage.deleteCommentData(post, comment)
                        database.deleteComment(comment) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, R.string.description_comment_deleted, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, R.string.label_error_occurred, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    R.id.reportTextView -> {

                    }
                    R.id.likeImgView -> {
                        reactComment(React.TYPE_LIKE, longClickPosition!!)
                    }
                    R.id.loveImgView -> {
                        reactComment(React.TYPE_LOVE, longClickPosition!!)
                    }
                    R.id.careImgView -> {
                        reactComment(React.TYPE_CARE, longClickPosition!!)
                    }
                    R.id.hahaImgView -> {
                        reactComment(React.TYPE_HAHA, longClickPosition!!)
                    }
                    R.id.wowImgView -> {
                        reactComment(React.TYPE_WOW, longClickPosition!!)
                    }
                    R.id.sadImgView -> {
                        reactComment(React.TYPE_SAD, longClickPosition!!)
                    }
                    R.id.angryImgView -> {
                        reactComment(React.TYPE_ANGRY, longClickPosition!!)
                    }
                }
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            // Fix BottomSheetDialog not showing after getting hidden when the user drags it down
            setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                val frameLayout = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!
                frameLayout.background = null
                BottomSheetBehavior.from(frameLayout).apply {
                    skipCollapsed = true
                }
            }

            val rootView = layoutInflater.inflate(R.layout.bottom_sheet_comment_actions, null).apply {
                likeImgView.setOnClickListener(bottomSheetEventListener)
                loveImgView.setOnClickListener(bottomSheetEventListener)
                careImgView.setOnClickListener(bottomSheetEventListener)
                hahaImgView.setOnClickListener(bottomSheetEventListener)
                wowImgView.setOnClickListener(bottomSheetEventListener)
                sadImgView.setOnClickListener(bottomSheetEventListener)
                angryImgView.setOnClickListener(bottomSheetEventListener)
                replyTextView.setOnClickListener(bottomSheetEventListener)
                copyTextView.setOnClickListener(bottomSheetEventListener)
                deleteTextView.setOnClickListener(bottomSheetEventListener)
                reportTextView.setOnClickListener(bottomSheetEventListener)
            }

            setContentView(rootView)
        }
    }

    override fun onViewsBound() {
        viewModel.liveIsLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                loadingAnimView.visibility = View.VISIBLE
            } else {
                loadingAnimView.visibility = View.GONE
            }
        })

        viewModel.liveComments.observe(viewLifecycleOwner, Observer {
            commentAdapter.submitList(it)
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.mediaPreviewImgView -> {
                startActivity(Intent(requireContext(), EmptyActivity::class.java).apply {
                    putExtra(EmptyActivity.KEY_FRAGMENT, BaseView.FRAGMENT_MEDIA)
                    putExtra(EmptyActivity.KEY_MEDIA, viewModel.myComment.media)
                    putExtra(EmptyActivity.KEY_MEDIAS, arrayListOf(viewModel.myComment.media))
                })
            }
            R.id.sendImgView -> {
                viewModel.myComment.createdTime = System.currentTimeMillis()
                viewModel.myComment.text = editText.text.toString()
                viewModel.sendComment { isSuccess ->
                    if (!isSuccess) {
                        Toast.makeText(requireContext(), getString(R.string.label_error_occurred), Toast.LENGTH_SHORT).show()
                    } else {
                        post.commentCount++
                    }
                }
                editText.setText("")
                viewModel.resetMyComment()
                updateSelectedMedia()
            }
            R.id.uploadImageImgView -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_IMAGE_REQUEST, ExternalIntentManager.CHOOSER_TYPE_IMAGE, false)
            }
            R.id.uploadVideoImgView -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_VIDEO_REQUEST, ExternalIntentManager.CHOOSER_TYPE_VIDEO, false)
            }
            R.id.discardMediaImgView -> {
                viewModel.myComment.media = null
                updateSelectedMedia()
            }
            R.id.closeImgView -> {
                dismiss()
            }
            R.id.avatarImgView -> {
                addProfileFragment(view.parent as View)
            }
            R.id.nameTextView -> {
                addProfileFragment(view.parent.parent as View)
            }
            R.id.reactImgView -> {
                val reactedType = React.TYPE_LOVE

                val curUser = sessionManager.curUser!!
                if (post.reacts[curUser.id] == null) {
                    database.reactPost(post, reactedType)
                    post.apply {
                        reacts[curUser.id!!] = React(
                                ownerId = curUser.id,
                                ownerName = curUser.name,
                                ownerAvatarUrl = curUser.avatarUrl,
                                type = reactedType,
                                createdTime = System.currentTimeMillis()
                        )
                        reactCount++
                    }

                    reactImgView.setImageResource(R.drawable.heart2)
                    val redTint = ContextCompat.getColor(requireContext(), R.color.missedCall)
                    ImageViewCompat.setImageTintList(reactImgView, ColorStateList.valueOf(redTint))
                } else {
                    database.unReactPost(post)
                    post.reacts.remove(curUser.id!!)
                    post.reactCount--

                    reactImgView.setImageResource(R.drawable.heart)
                    ImageViewCompat.setImageTintList(reactImgView, null)
                }
                reactCountTextView.text = post.reactCount.toString()
            }
            R.id.reactTextView -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)

                reactComment(React.TYPE_LOVE, position)
            }
            R.id.imageView -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val comment = commentAdapter.currentList[position]

                startActivity(Intent(requireContext(), EmptyActivity::class.java).apply {
                    putExtra(EmptyActivity.KEY_FRAGMENT, BaseView.FRAGMENT_MEDIA)
                    putExtra(EmptyActivity.KEY_MEDIA, comment.media)
                    putExtra(EmptyActivity.KEY_MEDIAS, arrayListOf(comment.media))
                })
            }
            R.id.replyTextView -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val comment = commentAdapter.currentList[position]
                zaloFragmentManager.addReplyFragment(comment, true)
            }
            R.id.headerLayout -> {
                zaloFragmentManager.addReactFragment(post.reacts)
            }
            R.id.reactLayout -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val comment = commentAdapter.currentList[position]
                zaloFragmentManager.addReactFragment(comment.reacts)
            }
            R.id.viewReplyTextView -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val comment = commentAdapter.currentList[position]
                zaloFragmentManager.addReplyFragment(comment)
            }
        }
    }

    override fun onLongClick(view: View): Boolean {
        when (view.id) {
            R.id.rootItemView -> {
                longClickPosition = recyclerView.getChildAdapterPosition(view)
                val comment = commentAdapter.currentList[longClickPosition!!]
                if (comment.ownerId == sessionManager.curUser!!.id) {
                    bottomSheetDialog.deleteTextView.visibility = View.VISIBLE
                    bottomSheetDialog.reportTextView.visibility = View.GONE
                } else {
                    bottomSheetDialog.deleteTextView.visibility = View.GONE
                    bottomSheetDialog.reportTextView.visibility = View.VISIBLE
                }
                bottomSheetDialog.show()
            }
            else -> {
                return false
            }
        }
        return true
    }

    private fun reactComment(reactedType: Int, position: Int) {
        val comment = commentAdapter.currentList[position]

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
        commentAdapter.notifyItemChanged(position, arrayListOf(Comment.PAYLOAD_METRICS))
    }

    private fun addProfileFragment(view: View) {
        val position = recyclerView.getChildAdapterPosition(view)
        val userId = commentAdapter.currentList[position].ownerId!!

        startActivity(Intent(requireContext(), EmptyActivity::class.java).apply {
            putExtra(EmptyActivity.KEY_FRAGMENT, BaseView.FRAGMENT_PROFILE)
            putExtra(EmptyActivity.KEY_USER_ID, userId)
        })
    }

    override fun onActivityResult(requestCode: Int, intent: Intent?) {
        utils.assertNotNull(intent, TAG, "onActivityResult.intent") { intentNotNull ->
            val uri = intentNotNull.data!!
            when (requestCode) {
                Constants.CHOOSE_IMAGE_REQUEST -> {
                    viewModel.createMedia(uri = uri.toString(), mediaType = Media.TYPE_IMAGE) {
                        viewModel.myComment.media = it
                        updateSelectedMedia()
                    }
                }
                Constants.CHOOSE_VIDEO_REQUEST -> {
                    viewModel.createMedia(uri = uri.toString(), mediaType = Media.TYPE_VIDEO) {
                        viewModel.myComment.media = it
                        updateSelectedMedia()
                    }
                }
            }
        }
    }

    private fun updateSelectedMedia() {
        if (viewModel.myComment.media != null) {
            val media = viewModel.myComment.media!!

            mediaPreviewImgView.layoutParams = mediaPreviewImgView.layoutParams.apply {
                this as ConstraintLayout.LayoutParams
                dimensionRatio = utils.getAdjustedRatio(media.ratio!!)
            }

            when (media) {
                is ImageMedia -> {
                    Picasso.get().smartLoad(media.uri, resourceManager, mediaPreviewImgView) {
                        it.fit().centerCrop()
                    }
                    playIcon.visibility = View.GONE
                }
                is VideoMedia -> {
                    resourceManager.getVideoThumbUri(media.uri!!) { uri ->
                        Picasso.get().smartLoad(uri, resourceManager, mediaPreviewImgView) {
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

    private fun checkEnableSend() {
        sendImgView.visibility = if (TextUtils.isEmpty(editText.text) && viewModel.myComment.media == null) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListeners.forEach { it.invoke() }
        dismissListeners.clear()
    }

    private val dismissListeners = arrayListOf<() -> Any>()

    fun addDismissListener(listener: () -> Any) {
        dismissListeners.add(listener)
    }

    fun removeDismissListener(listener: () -> Any) {
        dismissListeners.remove(listener)
    }

    override fun onFragmentResult(fragmentType: Int, result: Any?) {
        viewCommentPosition?.let { commentAdapter.notifyItemChanged(it, arrayListOf(Comment.PAYLOAD_METRICS)) }
    }
}