package com.mgt.zalo.ui.post_detail

import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.base.BaseView
import com.mgt.zalo.data_model.post.Diary
import com.mgt.zalo.data_model.react.React
import com.mgt.zalo.util.smartLoad
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_post_detail.*
import kotlinx.android.synthetic.main.part_post_actions.*
import javax.inject.Inject

class PostDetailFragment(
        val diary: Diary,
        val position:Int?
) : BaseFragment() {
//    private val viewModel: PostDetailViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var postMediaAdapter: PostMediaAdapter

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        // Inflate the layout for this fragment
        activity().showStatusBar()
        return inflater.inflate(R.layout.fragment_post_detail, container, false).apply {
            makeRoomForStatusBar(requireContext(), this)
        }
    }

    override fun onBindViews() {
        recyclerView.adapter = postMediaAdapter
        postMediaAdapter.submitList(diary.medias){
            position?.let {
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position,0)
            }
        }

        Picasso.get().smartLoad(diary.ownerAvatarUrl, resourceManager, avatarImgView) {
            it.fit().centerCrop()
        }

        nameTextView.text = diary.ownerName
        timeTextView.text = utils.getTimeDiffFormat(diary.createdTime!!)
        if (TextUtils.isEmpty(diary.text)) {
            descTextView.visibility = View.GONE
        } else {
            descTextView.text = diary.text
            descTextView.visibility = View.VISIBLE
        }

        updateMetrics()

        backImgView.setOnClickListener(this)
        moreImgView.setOnClickListener(this)
        avatarImgView.setOnClickListener(this)
        nameTextView.setOnClickListener(this)

        reactImgView.setOnClickListener{
            val reactedType = React.TYPE_LOVE

            val curUser = sessionManager.curUser!!
            if (diary.reacts[curUser.id] == null) {
                database.reactPost(diary, reactedType)
                diary.apply {
                    reacts[curUser.id!!] = React(
                            ownerId = curUser.id,
                            ownerName = curUser.name,
                            ownerAvatarUrl = curUser.avatarUrl,
                            type = reactedType,
                            createdTime = System.currentTimeMillis()
                    )
                    reactCount++
                }
            } else {
                database.unReactPost(diary)
                diary.reacts.remove(curUser.id!!)
                diary.reactCount--
            }
            updateMetrics()
        }

        reactCountTextView.setOnClickListener(this)
        commentImgView.setOnClickListener(this)
        commentCountTextView.setOnClickListener(this)
        shareImgView.setOnClickListener(this)
        shareCountTextView.setOnClickListener(this)
    }

    private fun updateMetrics(){
        reactCountTextView.text = utils.getMetricFormat(diary.reactCount)
        commentCountTextView.text = utils.getMetricFormat(diary.commentCount)
        shareCountTextView.text = utils.getMetricFormat(diary.shareCount)

        if (diary.reacts[sessionManager.curUser!!.id!!] != null) {
            reactImgView.setImageResource(R.drawable.heart2)
            val redTint = ContextCompat.getColor(requireContext(), R.color.missedCall)
            ImageViewCompat.setImageTintList(reactImgView, ColorStateList.valueOf(redTint))
        } else {
            reactImgView.setImageResource(R.drawable.heart)
            ImageViewCompat.setImageTintList(reactImgView, null)
        }
    }
//    override fun onViewsBound() {
//        viewModel.liveUser.observe(viewLifecycleOwner, Observer { user ->
//            Picasso.get().smartLoad(user.avatarUrl, resourceManager, avatarImgView) {
//                it.fit().centerCrop()
//            }
//
//            idTextView.text = user.id
//            nameTextView.text = user.name
//            postNumTextView.text = 400.toString()
//            followerNumTextView.text = 400.toString()
//            followingNumTextView.text = 400.toString()
//            descTextView.text = "what a lovely day !"
//            followedByTextView.text = "Theo dõi bởi thutrang20"
//
//            viewModel.listenForNewStory()
//        })
//
//        viewModel.liveStoryGroups.observe(viewLifecycleOwner, Observer { storyGroups ->
//            val recentStoryGroup = storyGroups.firstOrNull { it.id == StoryGroup.ID_RECENT_STORY_GROUP }
//
//            if (recentStoryGroup == null && viewModel.liveIsAnyStory.value == true) {
//                storyPreviewAdapter.submitList(storyGroups.toMutableList().apply {
//                    viewModel.liveUser.value?.let { user ->
//                        add(0, StoryGroup(
//                                id = StoryGroup.ID_RECENT_STORY_GROUP,
//                                ownerId = user.id,
//                                ownerName = user.name,
//                                ownerAvatarUrl = user.avatarUrl
//                        ))
//                    }
//                })
//            } else {
//                storyPreviewAdapter.submitList(storyGroups)
//            }
//        })
//
//        viewModel.liveIsAnyStory.observe(viewLifecycleOwner, Observer { isAnyStory ->
//            if (isAnyStory) {
//                viewModel.liveStoryGroups.value = viewModel.liveStoryGroups.value
//            }
//        })
//    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.backImgView -> {
                parent.onBackPressed()
            }
            R.id.moreImgView -> {
            }
            R.id.avatarImgView, R.id.nameTextView -> {
                fragmentManager().addProfileFragment(diary.ownerId!!)
            }
            R.id.imageView -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val media = postMediaAdapter.currentList[position]
                fragmentManager().addMediaFragment(media, diary.medias)
            }
            R.id.commentImgView->{
                fragmentManager().addCommentFragment(diary)
                fragmentManager().commentFragment!!.addDismissListener {
                    updateMetrics()
                }
            }
            R.id.reactImgView->{
//                val reactedType = React.TYPE_HEART
//
//                val curUser = sessionManager.curUser!!
//                if (diary.reacts[curUser.id] == null) {
//                    database.reactPost(post, reactedType)
//                    post.apply {
//                        reacts[curUser.id!!] = React(
//                                ownerId = curUser.id,
//                                ownerName = curUser.name,
//                                ownerAvatarUrl = curUser.avatarUrl,
//                                type = reactedType,
//                                createdTime = System.currentTimeMillis()
//                        )
//                        reactCount++
//                    }
//
//                    reactImgView.setImageResource(R.drawable.heart2)
//                    val redTint = ContextCompat.getColor(requireContext(), R.color.missedCall)
//                    ImageViewCompat.setImageTintList(reactImgView, ColorStateList.valueOf(redTint))
//                } else {
//                    database.unReactPost(post)
//                    post.reacts.remove(curUser.id!!)
//                    post.reactCount--
//
//                    reactImgView.setImageResource(R.drawable.heart)
//                    ImageViewCompat.setImageTintList(reactImgView, null)
//                }
//                reactCountTextView.text = post.reactCount.toString()
            }
        }
    }

    override fun onBackPressedCustomized(): Boolean {
        parent.onFragmentResult(BaseView.FRAGMENT_PROFILE, null)
        return false
    }

//    override fun getInstanceTag(): String {
//        return "${super.getInstanceTag()}$userId"
//    }
}