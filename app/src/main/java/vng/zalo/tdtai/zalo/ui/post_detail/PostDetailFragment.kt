package vng.zalo.tdtai.zalo.ui.post_detail

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_post_detail.*
import kotlinx.android.synthetic.main.part_post_actions.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.base.BaseView
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.util.smartLoad
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
        reactCountTextView.text = utils.getMetricFormat(diary.reactCount)
        commentCountTextView.text = utils.getMetricFormat(diary.reactCount)
        shareCountTextView.text = utils.getMetricFormat(diary.reactCount)

        backImgView.setOnClickListener(this)
        moreImgView.setOnClickListener(this)
        avatarImgView.setOnClickListener(this)
        nameTextView.setOnClickListener(this)

        reactImgView.setOnClickListener(this)
        reactCountTextView.setOnClickListener(this)
        commentImgView.setOnClickListener(this)
        commentCountTextView.setOnClickListener(this)
        shareImgView.setOnClickListener(this)
        shareCountTextView.setOnClickListener(this)
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
                parentZaloFragmentManager.removePostDetailFragment()
            }
            R.id.moreImgView -> {
            }
            R.id.avatarImgView, R.id.nameTextView -> {
                zaloFragmentManager.addProfileFragment(diary.ownerId!!)
            }
            R.id.imageView -> {
                val position = recyclerView.getChildAdapterPosition(view.parent as View)
                val media = postMediaAdapter.currentList[position]
                zaloFragmentManager.addMediaFragment(media, diary.medias)
            }
        }
    }

    override fun onBackPressedCustomized(): Boolean {
        parentZaloFragmentManager.removeProfileFragment()
        parent.onFragmentResult(BaseView.FRAGMENT_PROFILE, null)
        return true
    }

//    override fun getInstanceTag(): String {
//        return "${super.getInstanceTag()}$userId"
//    }
}