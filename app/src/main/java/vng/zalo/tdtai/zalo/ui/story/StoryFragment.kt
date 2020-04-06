package vng.zalo.tdtai.zalo.ui.story

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_add_highlight_story.*
import kotlinx.android.synthetic.main.bottom_add_highlight_story.view.*
import kotlinx.android.synthetic.main.fragment_story.*
import kotlinx.android.synthetic.main.item_story_base.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseActivity
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.common.StoryPreviewAdapter
import vng.zalo.tdtai.zalo.data_model.story.ImageStory
import vng.zalo.tdtai.zalo.data_model.story.Story
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.data_model.story.VideoStory
import vng.zalo.tdtai.zalo.repository.Storage
import vng.zalo.tdtai.zalo.util.animatePagerTransition
import vng.zalo.tdtai.zalo.widget.CubeOutTransformer
import javax.inject.Inject

class StoryFragment(
        private val activity: BaseActivity,
        val curStoryGroup: StoryGroup,
        val storyGroups: List<StoryGroup>
) : BaseFragment() {
    @Inject
    lateinit var storyGroupAdapter: StoryGroupAdapter

    @Inject
    lateinit var storyPreviewAdapter: StoryPreviewAdapter

    @Inject
    lateinit var createStoryGroupDialog: CreateStoryGroupDialog

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    private val viewModel: StoryViewModel by viewModels { viewModelFactory }

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var isBottomSheetShown = false

    private var lastPosition: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        activity.hideStatusBar()
        return layoutInflater.inflate(R.layout.fragment_story, container, false)
    }

    override fun onBindViews() {
        viewPager.apply {
            adapter = storyGroupAdapter

            setPageTransformer(CubeOutTransformer())

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (lastPosition == null) {
                        post {
                            focusCurrentItem()
                        }
                    }
                    if (lastPosition != position) {
                        loadMoreIfNeeded(viewPager.currentItem)
                    }
                    lastPosition = position
                }

                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        pauseCurrentItem()
                    } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        getCurrentItemView()?.let { itemView ->
                            if (lastPosition != viewPager.currentItem || itemView.playerView.player == null) {
                                focusCurrentItem()
                            } else {
                                resumeCurrentItem()
                            }
                        }
                        lastPosition = viewPager.currentItem
                    }
                }

                private fun loadMoreIfNeeded(position: Int) {
                    val minGap = 5
                    val isLeftReach = position < minGap
                    val isRightReach = storyGroupAdapter.itemCount - position < minGap - 1

                    viewModel.loadMoreStoryGroups(isLeftReach, isRightReach)
                }
            })
        }

        initBottomSheet()
    }

    override fun onViewsBound() {
        viewModel.liveStoryGroups.observe(viewLifecycleOwner, Observer { storyGroups ->
            storyGroupAdapter.submitList(storyGroups) {
                if (lastPosition == null) {
                    val curPosition = viewModel.liveStoryGroups.value!!.indexOfFirst { it.ownerId == curStoryGroup.ownerId && it.id == curStoryGroup.id }
                    viewPager.setCurrentItem(curPosition, false)
                    focusCurrentItem()
                }
            }
        })
    }

    private fun initBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            // Fix BottomSheetDialog not showing after getting hidden when the user drags it down
            setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                val frameLayout = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!.apply {
                    background = null
                }
                BottomSheetBehavior.from(frameLayout).apply {
                    skipCollapsed = true
                }

                pauseCurrentItem()

                isBottomSheetShown = true
            }
            setOnDismissListener {
                resumeCurrentItem()

                isBottomSheetShown = false
            }

            val rootView = layoutInflater.inflate(R.layout.bottom_add_highlight_story, null)
            rootView.cancelTextView.setOnClickListener(this@StoryFragment)
            rootView.recyclerView.adapter = storyPreviewAdapter

            setContentView(rootView)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isBottomSheetShown) {
            startOrResumeCurrentWatch()
        }
    }

    private fun startOrResumeCurrentWatch() {
        if (!storyGroupAdapter.isPreparing && exoPlayer.playbackState == ExoPlayer.STATE_READY) {
            resumeCurrentItem()
        } else {
            focusCurrentItem()
        }
    }

    override fun onPause() {
        super.onPause()
        pauseCurrentItem()
    }

    fun getCurrentItemPosition(): Int {
        return viewPager.currentItem
    }

    fun getCurrentItemView(): View? {
        val recyclerView = viewPager[0] as RecyclerView
        return recyclerView.findViewHolderForAdapterPosition(viewPager.currentItem)?.itemView
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop(true)
        activity.showStatusBar()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.closeImgView -> activity.removeStoryFragment()
            R.id.cancelTextView -> {
                bottomSheetDialog.dismiss()
            }
            R.id.markImgView -> {
                val curStoryGroup = storyGroupAdapter.currentList[viewPager.currentItem]
                val story = curStoryGroup.stories!![curStoryGroup.curPosition]

                storyPreviewAdapter.addedGroupsId = story.groupsId
                storyPreviewAdapter.notifyItemRangeChanged(0, storyPreviewAdapter.itemCount, arrayListOf(StoryGroup.PAYLOAD_ADDED_STATE))

                viewModel.getAllMyStoryGroups {
                    storyPreviewAdapter.submitList(it.toMutableList().apply {
                        add(0, StoryGroup(id = StoryGroup.ID_CREATE_STORY_GROUP))
                    })
                }

                bottomSheetDialog.show()
            }
            R.id.storyPreviewItemRoot -> {
                val position = bottomSheetDialog.recyclerView.getChildAdapterPosition(view)

                val curStoryGroup = storyGroupAdapter.currentList[viewPager.currentItem]
                val story = curStoryGroup.stories!![curStoryGroup.curPosition]

                val targetStoryGroup = storyPreviewAdapter.currentList[position] as StoryGroup

                if (targetStoryGroup.id == StoryGroup.ID_CREATE_STORY_GROUP) {
                    createStoryGroupDialog.show(childFragmentManager, story)
                } else {
                    processingDialog.show(childFragmentManager)

                    if (story.groupsId.contains(targetStoryGroup.id)) {
                        removeStoryFromGroup(story, targetStoryGroup)
                    } else {
                        addStoryToGroup(story, targetStoryGroup, false)
                    }
                }
            }
        }
    }

    fun nextStoryGroup() {
        val curGroupPosition = getCurrentItemPosition()
        if (curGroupPosition < storyGroupAdapter.itemCount - 1) {
            viewPager.animatePagerTransition(true)
        } else {
            activity.removeStoryFragment()
        }
    }

    fun previousStoryGroup() {
        val curGroupPosition = getCurrentItemPosition()
        if (curGroupPosition > 0) {
            viewPager.animatePagerTransition(false)
        }
    }

    fun addStoryToNewGroup(story: Story, newStoryGroupName: String) {
        processingDialog.show(childFragmentManager)

        val storyGroup = StoryGroup(
                id = database.getNewStoryGroupId(),
                name = newStoryGroupName,
                ownerId = sessionManager.curUser!!.id,
                ownerName = sessionManager.curUser!!.name,
                ownerAvatarUrl = sessionManager.curUser!!.avatarUrl,
                createdTime = System.currentTimeMillis()
        )

        when (story) {
            is VideoStory -> {
                resourceManager.getVideoThumbUri(story.videoUrl!!) { uri ->
                    val storyStoragePath = storage.getStoryGroupAvatarStoragePath(storyGroup.id!!)

                    storage.addFileAndGetDownloadUrl(
                            localPath = uri,
                            storagePath = storyStoragePath,
                            fileType = Storage.FILE_TYPE_IMAGE,
                            fileSize = resourceManager.getFileSize(uri),
                            onComplete = { downloadUrl ->
                                storyGroup.avatarUrl = downloadUrl

                                addStoryToGroup(story, storyGroup, true)
                            }
                    )
                }
            }
            is ImageStory -> {
                storyGroup.avatarUrl = story.imageUrl

                addStoryToGroup(story, storyGroup, true)
            }
        }
    }

    private fun addStoryToGroup(story: Story, storyGroup: StoryGroup, alsoCreateGroup: Boolean = false) {
        viewModel.addStoryToGroup(story, storyGroup, alsoCreateGroup) { isSuccess ->
            onOperationEnded(isSuccess, R.string.description_story_marked)

            if (isSuccess) {
                story.groupsId = story.groupsId.toMutableList().apply { add(storyGroup.id!!) }
            }
        }
    }

    private fun removeStoryFromGroup(story: Story, storyGroup: StoryGroup) {
        viewModel.removeStoryFromGroup(story, storyGroup) { isSuccess ->
            onOperationEnded(isSuccess, R.string.description_story_unmarked)

            if (isSuccess) {
                story.groupsId = story.groupsId.toMutableList().apply { remove(storyGroup.id!!) }
            }
        }
    }

    private fun onOperationEnded(isSuccess: Boolean, @IdRes successToastStringResId: Int) {
        Toast.makeText(requireContext(),
                getString(
                        if (isSuccess) {
                            bottomSheetDialog.dismiss()
                            if (createStoryGroupDialog.parentFragment != null) {
                                createStoryGroupDialog.dismiss()
                            }

                            successToastStringResId
                        } else {
                            R.string.label_error_occurred
                        }
                ), Toast.LENGTH_SHORT).show()

        resumeCurrentItem()

        processingDialog.dismiss()
    }

    fun pauseCurrentItem() {
        getCurrentItemView()?.let {
            storyGroupAdapter.onStoryGroupPause(it, getCurrentItemPosition())
        }
    }

    fun resumeCurrentItem() {
        getCurrentItemView()?.let {
            storyGroupAdapter.onStoryGroupResume(it, getCurrentItemPosition())
        }
    }

    fun focusCurrentItem() {
        getCurrentItemView()?.let {
            storyGroupAdapter.onStoryGroupFocused(it, getCurrentItemPosition())
        }
    }

//    override fun getInstanceTag(): String {
//        return "${super.getInstanceTag()}${curStoryGroup.ownerId}${curStoryGroup.id}"
//    }
}